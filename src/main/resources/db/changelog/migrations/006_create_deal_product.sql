--liquibase formatted sql
--changeset your-name:BCORE-33-1

CREATE TABLE deal_product
(
    id         UUID PRIMARY KEY NOT NULL,
    deal_id    UUID             NOT NULL,
    product_id UUID             NOT NULL,
    quantity   INTEGER          NOT NULL,
    unit_price DECIMAL(15, 2)   NOT NULL,

    CONSTRAINT fk_deal_product_deal FOREIGN KEY (deal_id) REFERENCES deals (id) ON DELETE CASCADE,
    CONSTRAINT fk_deal_product_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    CONSTRAINT uk_deal_product UNIQUE (deal_id, product_id)
);

CREATE INDEX idx_deal_product_deal_id ON deal_product (deal_id);
CREATE INDEX idx_deal_product_product_id ON deal_product (product_id);