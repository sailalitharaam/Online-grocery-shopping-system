package edu.vce.grocery;
import org.mindrot.jbcrypt.BCrypt;import javax.servlet.*;import javax.servlet.annotation.WebServlet;import javax.servlet.http.*;import java.io.IOException;import java.math.BigDecimal;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.sql.*;import java.util.*;
@WebServlet(urlPatterns={"/","/store"})
public class GroceryServlet extends HttpServlet {
 @SuppressWarnings("unchecked") private Map<Integer,CartLine> cart(HttpSession s){Map<Integer,CartLine> c=(Map<Integer,CartLine>)s.getAttribute("cart");if(c==null){c=new LinkedHashMap<>();s.setAttribute("cart",c);}return c;}
 private String esc(String x){return x==null?"":x.trim();}
 private void msg(HttpServletRequest r,String m){r.setAttribute("message",m);}
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
  String action=Optional.ofNullable(req.getParameter("action")).orElse("shop");
  try(Connection c=Db.get()){
   if("orders".equals(action)){if(req.getSession().getAttribute("customer")==null){res.sendRedirect("?action=login");return;} loadOrders(c,req);req.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(req,res);return;}
   if("cart".equals(action)){req.setAttribute("cart",cart(req.getSession()));req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req,res);return;}
   if("login".equals(action)||"register".equals(action)){req.getRequestDispatcher("/WEB-INF/views/"+action+".jsp").forward(req,res);return;}
   loadProducts(c,req);req.getRequestDispatcher("/WEB-INF/views/shop.jsp").forward(req,res);
  }catch(SQLException e){throw new ServletException("Unable to load FreshCart data. Check database configuration.",e);}
 }
 @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
  req.setCharacterEncoding("UTF-8");String action=Optional.ofNullable(req.getParameter("action")).orElse("");
  try(Connection c=Db.get()){
   switch(action){
    case "register": register(c,req,res);return;
    case "login": login(c,req,res);return;
    case "logout": req.getSession().invalidate();res.sendRedirect(req.getContextPath()+"/");return;
    case "add": add(c,req);break;
    case "quantity": quantity(req);break;
    case "remove": cart(req.getSession()).remove(Integer.parseInt(req.getParameter("id")));break;
    case "checkout": checkout(c,req);break;
    default: msg(req,"Unknown request.");
   }
  }catch(NumberFormatException e){msg(req,"Please enter a valid quantity.");}catch(SQLException e){throw new ServletException("The request could not be saved. Check database configuration.",e);}
  if("checkout".equals(action)&&req.getAttribute("message")!=null){req.setAttribute("cart",cart(req.getSession()));req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req,res);return;}
  res.sendRedirect(req.getContextPath()+"/"+(("orders".equals(action)||"checkout".equals(action))?"?action=orders":"?action=cart"));
 }
 private void loadProducts(Connection c,HttpServletRequest r)throws SQLException{
  String q=esc(r.getParameter("q")),cat=esc(r.getParameter("category"));String sql="SELECT p.product_id,p.name,c.name category,p.unit,p.price,p.stock FROM products p JOIN categories c ON c.category_id=p.category_id WHERE p.active=1 AND p.stock>0 AND p.name LIKE ? AND (?='' OR c.name=?) ORDER BY p.name";
  List<Product> ps=new ArrayList<>();try(PreparedStatement s=c.prepareStatement(sql)){s.setString(1,"%"+q+"%");s.setString(2,cat);s.setString(3,cat);try(ResultSet x=s.executeQuery()){while(x.next())ps.add(new Product(x.getInt("product_id"),x.getString("name"),x.getString("category"),x.getString("unit"),x.getBigDecimal("price"),x.getInt("stock")));}}
  List<String> cats=new ArrayList<>();try(Statement s=c.createStatement();ResultSet x=s.executeQuery("SELECT name FROM categories ORDER BY name")){while(x.next())cats.add(x.getString(1));}r.setAttribute("products",ps);r.setAttribute("categories",cats);r.setAttribute("query",q);r.setAttribute("selectedCategory",cat);
 }
 private void register(Connection c,HttpServletRequest r,HttpServletResponse response)throws SQLException,ServletException,IOException{
  String name=esc(r.getParameter("name")),email=esc(r.getParameter("email")).toLowerCase(Locale.ROOT),pass=Optional.ofNullable(r.getParameter("password")).orElse("");
  if(name.isEmpty()||!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")||pass.length()<8){msg(r,"Enter a name, valid email and password of at least 8 characters.");r.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(r,response);return;}
  try(PreparedStatement s=c.prepareStatement("INSERT INTO customers(full_name,email,password_hash) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)){s.setString(1,name);s.setString(2,email);s.setString(3,BCrypt.hashpw(pass,BCrypt.gensalt(12)));s.executeUpdate();try(ResultSet k=s.getGeneratedKeys()){k.next();r.getSession().setAttribute("customer",new Customer(k.getInt(1),name,email));}}
  response.sendRedirect(r.getContextPath()+"/");
 }
 private void login(Connection c,HttpServletRequest r,HttpServletResponse response)throws SQLException,ServletException,IOException{
  String email=esc(r.getParameter("email")).toLowerCase(Locale.ROOT),pass=Optional.ofNullable(r.getParameter("password")).orElse("");
  try(PreparedStatement s=c.prepareStatement("SELECT customer_id,full_name,email,password_hash FROM customers WHERE email=?")){s.setString(1,email);try(ResultSet x=s.executeQuery()){if(x.next()&&BCrypt.checkpw(pass,x.getString("password_hash"))){r.getSession().setAttribute("customer",new Customer(x.getInt(1),x.getString(2),x.getString(3)));response.sendRedirect(r.getContextPath()+"/");return;}}}
  msg(r,"Email or password was not recognized.");r.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(r,response);
 }
 
 private void add(Connection c,HttpServletRequest r)throws SQLException{int id=Integer.parseInt(r.getParameter("id"));Product p=findProduct(c,id);if(p!=null){Map<Integer,CartLine> cart=cart(r.getSession());CartLine line=cart.get(id);int qty=line==null?0:line.getQuantity();if(qty<p.getStock())cart.put(id,line==null?new CartLine(p,1):new CartLine(p,qty+1));}}
 private Product findProduct(Connection c,int id)throws SQLException{try(PreparedStatement s=c.prepareStatement("SELECT p.product_id,p.name,c.name category,p.unit,p.price,p.stock FROM products p JOIN categories c ON c.category_id=p.category_id WHERE p.product_id=? AND p.active=1")){s.setInt(1,id);try(ResultSet x=s.executeQuery()){return x.next()?new Product(x.getInt(1),x.getString(2),x.getString(3),x.getString(4),x.getBigDecimal(5),x.getInt(6)):null;}}}
 private void quantity(HttpServletRequest r){int id=Integer.parseInt(r.getParameter("id")),qty=Integer.parseInt(r.getParameter("qty"));CartLine l=cart(r.getSession()).get(id);if(l!=null){if(qty<=0)cart(r.getSession()).remove(id);else if(qty<=l.getProduct().getStock())l.setQuantity(qty);}}
 private void checkout(Connection c,HttpServletRequest r)throws SQLException{
  Customer who=(Customer)r.getSession().getAttribute("customer");Map<Integer,CartLine> cart=cart(r.getSession());String addr=esc(r.getParameter("address"));if(who==null){msg(r,"Please sign in before checkout.");return;}if(cart.isEmpty()||addr.length()<8){msg(r,"Your cart needs items and a complete delivery address.");return;}
  BigDecimal total=cart.values().stream().map(CartLine::getSubtotal).reduce(BigDecimal.ZERO,BigDecimal::add);c.setAutoCommit(false);
  try{long oid;try(PreparedStatement s=c.prepareStatement("INSERT INTO orders(customer_id,address,payment_method,payment_status,order_status,total) VALUES(?,?,'CASH_ON_DELIVERY','PENDING','CONFIRMED',?)",Statement.RETURN_GENERATED_KEYS)){s.setInt(1,who.getId());s.setString(2,addr);s.setBigDecimal(3,total);s.executeUpdate();try(ResultSet k=s.getGeneratedKeys()){k.next();oid=k.getLong(1);}}
   for(CartLine l:cart.values()){try(PreparedStatement s=c.prepareStatement("UPDATE products SET stock=stock-? WHERE product_id=? AND stock>=?")){s.setInt(1,l.getQuantity());s.setInt(2,l.getProduct().getId());s.setInt(3,l.getQuantity());if(s.executeUpdate()!=1)throw new SQLException("Stock changed; please refresh the cart.");}
    try(PreparedStatement s=c.prepareStatement("INSERT INTO order_items(order_id,product_id,product_name,unit_price,quantity) VALUES(?,?,?,?,?)")){s.setLong(1,oid);s.setInt(2,l.getProduct().getId());s.setString(3,l.getProduct().getName());s.setBigDecimal(4,l.getProduct().getPrice());s.setInt(5,l.getQuantity());s.executeUpdate();}}
   c.commit();cart.clear();r.setAttribute("message","Order #"+oid+" placed. Payment is cash on delivery.");
  }catch(SQLException e){c.rollback();msg(r,"Order could not be placed: "+e.getMessage());}finally{c.setAutoCommit(true);}
 }
 private void loadOrders(Connection c,HttpServletRequest r)throws SQLException{Customer who=(Customer)r.getSession().getAttribute("customer");List<Map<String,Object>> orders=new ArrayList<>();try(PreparedStatement s=c.prepareStatement("SELECT order_id,total,order_status,payment_status,created_at,address FROM orders WHERE customer_id=? ORDER BY created_at DESC")){s.setInt(1,who.getId());try(ResultSet x=s.executeQuery()){while(x.next()){Map<String,Object> o=new HashMap<>();o.put("id",x.getLong("order_id"));o.put("total",x.getBigDecimal("total"));o.put("status",x.getString("order_status"));o.put("payment",x.getString("payment_status"));o.put("date",x.getTimestamp("created_at"));o.put("address",x.getString("address"));orders.add(o);}}}r.setAttribute("orders",orders);}
}


