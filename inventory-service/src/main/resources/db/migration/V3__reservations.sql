ALTER TABLE t_inventory MODIFY sku_code VARCHAR(64) NOT NULL, MODIFY quantity INT NOT NULL;
ALTER TABLE t_inventory ADD CONSTRAINT uk_inventory_sku UNIQUE (sku_code);
ALTER TABLE t_inventory ADD CONSTRAINT ck_inventory_quantity CHECK (quantity >= 0);
CREATE TABLE t_reservations (
    order_number VARCHAR(36) PRIMARY KEY,
    sku_code VARCHAR(64) NOT NULL,
    quantity INT NOT NULL,
    accepted BOOLEAN NOT NULL,
    CONSTRAINT ck_reservation_quantity CHECK (quantity > 0)
);
