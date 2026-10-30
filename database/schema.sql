CREATE DATABASE IF NOT EXISTS freshcart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE freshcart;
CREATE TABLE customers (customer_id INT AUTO_INCREMENT PRIMARY KEY, full_name VARCHAR(100) NOT NULL, email VARCHAR(160) NOT NULL UNIQUE, password_hash VARCHAR(60) NOT NULL, phone VARCHAR(20), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE categories (category_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(60) NOT NULL UNIQUE);
CREATE TABLE products (product_id INT AUTO_INCREMENT PRIMARY KEY, category_id INT NOT NULL, name VARCHAR(120) NOT NULL, unit VARCHAR(60) NOT NULL, price DECIMAL(10,2) NOT NULL, stock INT NOT NULL DEFAULT 0, active BOOLEAN NOT NULL DEFAULT TRUE, FOREIGN KEY(category_id) REFERENCES categories(category_id));
CREATE TABLE orders (order_id BIGINT AUTO_INCREMENT PRIMARY KEY, customer_id INT NOT NULL, address VARCHAR(500) NOT NULL, payment_method VARCHAR(30) NOT NULL, payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING', order_status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED', total DECIMAL(10,2) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(customer_id) REFERENCES customers(customer_id));
CREATE TABLE order_items (order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL, product_id INT NOT NULL, product_name VARCHAR(120) NOT NULL, unit_price DECIMAL(10,2) NOT NULL, quantity INT NOT NULL, FOREIGN KEY(order_id) REFERENCES orders(order_id) ON DELETE CASCADE, FOREIGN KEY(product_id) REFERENCES products(product_id));
INSERT INTO categories(name) VALUES ('Fruits'),('Vegetables'),('Dairy & Eggs'),('Bakery'),('Pantry');
INSERT INTO products(category_id,name,unit,price,stock) VALUES
(1,'Alphonso Mango','1 kg',189,35),(1,'Red Apples','4 pcs',149,50),(1,'Banana','6 pcs',59,80),(1,'Avocado','2 pcs',129,20),
(2,'Spinach','1 bunch',35,40),(2,'Tomatoes','500 g',32,70),(2,'Broccoli','1 pc',75,25),(2,'Carrots','500 g',42,60),
(3,'Whole Milk','1 L',68,45),(3,'Farm Eggs','6 pcs',84,30),(3,'Greek Yogurt','400 g',115,20),
(4,'Sourdough Bread','400 g',95,18),(5,'Basmati Rice','1 kg',110,35),(5,'Toor Dal','1 kg',145,28);

