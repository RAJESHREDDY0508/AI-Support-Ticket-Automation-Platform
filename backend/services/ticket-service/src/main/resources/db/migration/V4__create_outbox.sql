CREATE TABLE outbox (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id        UUID NOT NULL UNIQUE,
    event_type      VARCHAR(100) NOT NULL,
    topic           VARCHAR(255) NOT NULL,
    schema_version  VARCHAR(20) NOT NULL DEFAULT '1',
    occurred_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id  VARCHAR(255),
    payload         JSONB NOT NULL,
    published_at    TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message   TEXT,
    retry_count     INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_outbox_published_at ON outbox(published_at);
CREATE INDEX idx_outbox_created_at_unpublished ON outbox(created_at) WHERE published_at IS NULL;
