CREATE TABLE ticket_ai_suggestions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id           UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    suggested_category  VARCHAR(50) NOT NULL,
    suggested_priority  VARCHAR(50) NOT NULL,
    suggested_response  TEXT,
    confidence_score    VARCHAR(50) NOT NULL,
    confidence_percent  INT,
    reasoning           TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ticket_ai_suggestions_ticket_id ON ticket_ai_suggestions(ticket_id);
