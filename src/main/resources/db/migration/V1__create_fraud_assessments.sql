CREATE TABLE fraud_assessments (
  id UUID PRIMARY KEY,
  transaction_id VARCHAR(80) NOT NULL,
  customer_id VARCHAR(80) NOT NULL,
  decision VARCHAR(10) NOT NULL CHECK (decision IN ('APPROVE','REVIEW','BLOCK')),
  risk_score SMALLINT NOT NULL CHECK (risk_score BETWEEN 0 AND 100),
  reason_codes VARCHAR(500) NOT NULL,
  assessed_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT uk_fraud_assessment_transaction UNIQUE (transaction_id)
);
CREATE INDEX idx_fraud_assessments_customer_time ON fraud_assessments(customer_id, assessed_at DESC);
CREATE INDEX idx_fraud_assessments_decision_time ON fraud_assessments(decision, assessed_at DESC);
