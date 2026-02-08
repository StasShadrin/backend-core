--liquibase formatted sql
--changeset your-name:BCORE-32-2

CREATE TABLE IF NOT EXISTS leads
(
    id         UUID PRIMARY KEY,
    version    BIGINT                   NOT NULL DEFAULT 0,
    name       VARCHAR(255)             NOT NULL,
    email      VARCHAR(255)             NOT NULL UNIQUE,
    phone      VARCHAR(50)              NOT NULL,
    company_id UUID REFERENCES companies (id),
    status     VARCHAR(50)              NOT NULL,
--     source      VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
--     updated_at  TIMESTAMP WITH TIME ZONE,
--     created_by  UUID,
--     assigned_to UUID
);

-- Индекс для быстрого поиска по email
CREATE INDEX IF NOT EXISTS idx_leads_email ON leads (email);

-- Индекс для фильтрации по статусу
CREATE INDEX IF NOT EXISTS idx_leads_status ON leads (status);

-- Индекс для фильтрации по компании
CREATE INDEX IF NOT EXISTS idx_leads_company_id ON leads (company_id);