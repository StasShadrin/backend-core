--liquibase formatted sql
--changeset your-name:BCORE-36-1

ALTER TABLE leads
ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;