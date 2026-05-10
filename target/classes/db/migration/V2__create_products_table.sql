-- Products catalog table
CREATE TABLE products (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL,
    sku         VARCHAR(50)     NOT NULL UNIQUE,
    description TEXT,
    price       NUMERIC(10, 2)  NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_price CHECK (price > 0)
);

CREATE INDEX idx_products_sku  ON products (sku);
CREATE INDEX idx_products_name ON products (name);
