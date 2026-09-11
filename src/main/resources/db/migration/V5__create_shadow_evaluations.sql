CREATE TABLE fraud_shadow_evaluations (
    id UUID PRIMARY KEY,
    transaction_id VARCHAR(80) NOT NULL,
    experiment_version VARCHAR(80) NOT NULL,
    champion_decision VARCHAR(10) NOT NULL,
    challenger_decision VARCHAR(10) NOT NULL,
    risk_score INTEGER NOT NULL,
    diverged BOOLEAN NOT NULL,
    evaluated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_shadow_transaction_version UNIQUE (transaction_id, experiment_version),
    CONSTRAINT ck_shadow_champion CHECK (champion_decision IN ('APPROVE', 'REVIEW', 'BLOCK')),
    CONSTRAINT ck_shadow_challenger CHECK (challenger_decision IN ('APPROVE', 'REVIEW', 'BLOCK')),
    CONSTRAINT ck_shadow_score CHECK (risk_score BETWEEN 0 AND 100)
);

CREATE INDEX idx_shadow_version_time
    ON fraud_shadow_evaluations (experiment_version, evaluated_at DESC);

CREATE INDEX idx_shadow_diverged_time
    ON fraud_shadow_evaluations (diverged, evaluated_at DESC);
