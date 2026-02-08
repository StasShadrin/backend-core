--liquibase formatted sql
--changeset your-name:BCORE-32-1

CREATE TABLE IF NOT EXISTS companies
(
    id       UUID PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    industry VARCHAR(100)
);

-- Индекс для поиска по названию компании
CREATE INDEX IF NOT EXISTS idx_companies_name ON companies (name);