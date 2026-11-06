ALTER TABLE notifications DROP CONSTRAINT CK_notifications_audience;

ALTER TABLE notifications ADD customer_id INT NULL;

ALTER TABLE notifications ADD CONSTRAINT FK_notifications_customer
    FOREIGN KEY (customer_id) REFERENCES customers(id);

ALTER TABLE notifications ADD CONSTRAINT CK_notifications_audience
    CHECK (audience IN ('ADMIN', 'SALONIST', 'CUSTOMER'));

CREATE INDEX IX_notifications_customer
    ON notifications (customer_id, created_at);

CREATE TABLE client_notes (
    id INT IDENTITY(1,1) PRIMARY KEY,
    customer_id INT NOT NULL,
    body NVARCHAR(1000) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_client_notes_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX IX_client_notes_customer
    ON client_notes (customer_id, created_at);
