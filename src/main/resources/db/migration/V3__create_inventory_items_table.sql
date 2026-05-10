-- Inventory items table (links products to stock locations)
CREATE TABLE inventory_items (
    id           BIGSERIAL    PRIMARY KEY,
    product_id   BIGINT       NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    quantity     INT          NOT NULL DEFAULT 0,
    location     VARCHAR(100),
    last_updated TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_quantity CHECK (quantity >= 0)
);

CREATE INDEX idx_inventory_product  ON inventory_items (product_id);
CREATE INDEX idx_inventory_location ON inventory_items (location);
