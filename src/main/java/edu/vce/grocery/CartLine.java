package edu.vce.grocery;
import java.math.BigDecimal;
public class CartLine {
 private final Product product; private int quantity;
 public CartLine(Product p,int q){product=p;quantity=q;} public Product getProduct(){return product;} public int getQuantity(){return quantity;} public void setQuantity(int q){quantity=q;} public BigDecimal getSubtotal(){return product.getPrice().multiply(BigDecimal.valueOf(quantity));}
}
