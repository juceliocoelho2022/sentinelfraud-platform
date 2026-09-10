CREATE TABLE processed_events (
    id UUID PRIMARY KEY,
    aggregate_id VARCHAR(100) NOT NULL,
    source_topic VARCHAR(200) NOT NULL,
    decision VARCHAR(20) NOT NULL,
    risk_score INTEGER NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_processed_event_aggregate UNIQUE (aggregate_id)
);

CREATE TABLE dead_letter_events (
    id UUID PRIMARY KEY,
    aggregate_id VARCHAR(100) NOT NULL,
    original_topic VARCHAR(200) NOT NULL,
    payload TEXT NOT NULL,
    error_message VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    replayed_at TIMESTAMPTZ,
    CONSTRAINT uk_dead_letter_aggregate UNIQUE (aggregate_id),
    CONSTRAINT ck_dead_letter_status CHECK (status IN ('PENDING', 'REPLAYED'))
);

CREATE INDEX idx_dead_letter_status_created ON dead_letter_events (status, created_at);
