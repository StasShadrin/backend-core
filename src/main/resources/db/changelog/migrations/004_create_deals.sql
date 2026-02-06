--liquibase formatted sql
--changeset your-name:BCORE-32-4

CREATE TABLE IF NOT EXISTS deals
(
    id         UUID PRIMARY KEY,
    version    BIGINT                   NOT NULL DEFAULT 0,
    lead_id    UUID                     REFERENCES leads (id) ON DELETE SET NULL,
    title      VARCHAR(255)             NOT NULL,
    amount     DECIMAL(15, 2)           NOT NULL,
--     currency            VARCHAR(3)                        DEFAULT 'USD',
    status     VARCHAR(50)              NOT NULL,
--     company_id         UUID REFERENCES companies(id),
--     probability         INTEGER CHECK (probability BETWEEN 0 AND 100),
--     expected_close_date DATE,
--     actual_close_date   DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
--     updated_at          TIMESTAMP WITH TIME ZONE,
--     assigned_to         UUID
);

-- Индексы для deals
CREATE INDEX IF NOT EXISTS idx_deals_lead_id ON deals (lead_id);
CREATE INDEX IF NOT EXISTS idx_deals_status ON deals (status);