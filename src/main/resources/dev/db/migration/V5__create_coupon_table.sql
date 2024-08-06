CREATE TABLE IF NOT EXISTS coupons (
  id INT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT true
);

ALTER TABLE orders -- 1 coupon áp dụng cho cả order
ADD COLUMN coupon_id INT,
ADD CONSTRAINT fk_orders_coupon
FOREIGN KEY (coupon_id) REFERENCES coupons(id);

ALTER TABLE order_details -- 1 coupon có thể chỉ áp dụng cho 1 số product nhất định
ADD COLUMN coupon_id INT,
ADD CONSTRAINT fk_order_details_coupon
FOREIGN KEY (coupon_id) REFERENCES coupons(id);

CREATE TABLE IF NOT EXISTS coupon_conditions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  coupon_id INT NOT NULL,
  attribute VARCHAR(255) NOT NULL,
  operator VARCHAR(10) NOT NULL, -- điều kiện
  value VARCHAR(255) NOT NULL,
  discount_amount DECIMAL(5, 2) NOT NULL, -- discount bn %
  FOREIGN KEY (coupon_id) REFERENCES coupons(id)
);
--INSERT INTO coupons(id, code) VALUES (1, 'HEAVEN');
--INSERT INTO coupons(id, code) VALUES (2, 'DISCOUNT20');
--
--INSERT INTO coupon_conditions (id, coupon_id, attribute, operator, value, discount_amount)
--VALUES (1, 1, 'minimum_amount', '>', '100', 10);
--
--INSERT INTO coupon_conditions (id, coupon_id, attribute, operator, value, discount_amount)
--VALUES (2, 1, 'applicable_date', 'BETWEEN', '2023-12-25', 5);
--
--INSERT INTO coupon_conditions (id, coupon_id, attribute, operator, value, discount_amount)
--VALUES (3, 2, 'minimum_amount', '>', '200', 20);
--
--Nếu đơn hàng có tổng giá trị là 120 đô la và áp dụng coupon 1
--Giá trị sau khi áp dụng giảm giá 10%:
--120 đô la * (1 - 10/100) = 120 đô la * 0.9 = 108 đô la
--
--Giá trị sau khi áp dụng giảm giá 5%:
--108 đô la * (1 - 5/100) = 108 đô la * 0.95 = 102.6 đô la

