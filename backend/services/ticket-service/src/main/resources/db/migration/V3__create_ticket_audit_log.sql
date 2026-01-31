CREATE TABLE ticket_audit_log (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id   UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    action      VARCHAR(100) NOT NULL,
    old_value   TEXT,
    new_value   TEXT,
    changed_by  VARCHAR(255),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ticket_audit_log_ticket_id ON ticket_audit_log(ticket_id);
CREATE INDEX idx_ticket_audit_log_created_at ON ticket_audit_log(created_at DESC);
