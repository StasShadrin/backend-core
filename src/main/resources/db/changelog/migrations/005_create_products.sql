--liquibase formatted sql
--changeset your-name:BCORE-32-5

CREATE TABLE IF NOT EXISTS products
(
    id     UUID PRIMARY KEY    NOT NULL,
    name   VARCHAR(255)        NOT NULL,             -- название товара
    sku    VARCHAR(100) UNIQUE NOT NULL,             -- уникальный артикул (Stock Keeping Unit)
    price  DECIMAL(19, 2)      NOT NULL,             -- цена товара
    active BOOLEAN             NOT NULL DEFAULT TRUE -- признак активности
);