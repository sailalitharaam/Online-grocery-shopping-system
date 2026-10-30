package edu.vce.grocery;
import java.sql.*;
public final class Db {
 private Db(){}
 public static Connection get() throws SQLException {
  String url=System.getenv().getOrDefault("FRESHCART_JDBC_URL","jdbc:mysql://localhost:3306/freshcart?useSSL=false&serverTimezone=UTC");
  String user=System.getenv().getOrDefault("FRESHCART_DB_USER","root");
  String pass=System.getenv().getOrDefault("FRESHCART_DB_PASSWORD","");
  return DriverManager.getConnection(url,user,pass);
 }
}
