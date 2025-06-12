CREATE TABLE mt564_events_audit (
    event_reference VARCHAR(64),
    financial_instrument_id VARCHAR(64),
    sender_bic VARCHAR(20),
    version_number INT,
    business_hash VARCHAR(64),
    updated_at TIMESTAMP,
    snapshot JSONB,  -- Full snapshot of event attributes
    PRIMARY KEY (event_reference, financial_instrument_id, sender_bic, version_number)
);
