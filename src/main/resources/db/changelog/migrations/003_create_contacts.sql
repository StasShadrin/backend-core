--liquibase formatted sql
--changeset your-name:BCORE-32-3

CREATE TABLE IF NOT EXISTS contacts
(
    id         UUID PRIMARY KEY,
    lead_id    UUID REFERENCES leads (id) ON DELETE CASCADE,
    first_name VARCHAR(255)             NOT NULL,
    last_name  VARCHAR(255)             NOT NULL,
    email      VARCHAR(255)             NOT NULL,
    phone      VARCHAR(50),
    position   VARCHAR(100),
    is_primary BOOLEAN                           DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Индекс для быстрого поиска контактов по lead_id
CREATE INDEX IF NOT EXISTS idx_contacts_lead_id ON contacts (lead_id);