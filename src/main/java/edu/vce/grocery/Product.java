package edu.vce.grocery;
import java.math.BigDecimal;
public class Product {
 private int id,stock; private String name,category,unit; private BigDecimal price;
 public Product(int id,String name,String category,String unit,BigDecimal price,int stock){this.id=id;this.name=name;this.category=category;this.unit=unit;this.price=price;this.stock=stock;}
 public int getId(){return id;} public int getStock(){return stock;} public String getName(){return name;} public String getCategory(){return category;} public String getUnit(){return unit;} public BigDecimal getPrice(){return price;}
}
