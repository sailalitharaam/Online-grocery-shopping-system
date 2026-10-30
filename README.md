# FreshCart

A JSP, Java Servlet, JDBC and MySQL academic web application prototype for the assigned online grocery shopping topic.

## Requirements
- Java 11+
- Apache Maven 3.8+
- Apache Tomcat 9
- MySQL 8

## Setup
1. Create the sample database with `mysql -u root -p < database/schema.sql`.
2. Configure `FRESHCART_JDBC_URL`, `FRESHCART_DB_USER` and `FRESHCART_DB_PASSWORD` in Tomcat's environment. Defaults assume local MySQL and user `root`.
3. Build from this folder using `mvn clean package`.
4. Copy `target/freshcart.war` to Tomcat 9 `webapps/`, start Tomcat, and open `/freshcart/`.

The Java web application uses JSP views under `WEB-INF/views`, a servlet controller, prepared JDBC statements and BCrypt password hashes. Checkout supports cash on delivery for the demo. It does not charge a card or call a delivery provider. Set strong database credentials and add CSRF protection, rate limiting, and deployment-specific security review before any non-demo use.

`index.html` at the project root is a standalone frontend-only preview. The Maven project under `src/` is the source implementation for the assigned JSP/Servlet/JDBC stack.
