ALTER TABLE fraud_shadow_evaluations
    ADD COLUMN effective_decision VARCHAR(10),
    ADD COLUMN rollout_bucket INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN promoted BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE fraud_shadow_evaluations
SET effective_decision = champion_decision
WHERE effective_decision IS NULL;

ALTER TABLE fraud_shadow_evaluations
    ALTER COLUMN effective_decision SET NOT NULL,
    ADD CONSTRAINT ck_shadow_effective
        CHECK (effective_decision IN ('APPROVE', 'REVIEW', 'BLOCK')),
    ADD CONSTRAINT ck_shadow_rollout_bucket
        CHECK (rollout_bucket BETWEEN 0 AND 99);

CREATE INDEX idx_shadow_promoted_time
    ON fraud_shadow_evaluations (promoted, evaluated_at DESC);
