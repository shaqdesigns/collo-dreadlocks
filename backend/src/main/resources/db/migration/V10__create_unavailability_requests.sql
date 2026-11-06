CREATE TABLE unavailability_requests (
    id INT IDENTITY(1,1) PRIMARY KEY,
    salonist_id INT NOT NULL,
    start_at DATETIME2 NOT NULL,
    end_at DATETIME2 NOT NULL,
    reason NVARCHAR(500) NULL,
    status NVARCHAR(20) NOT NULL,
    admin_note NVARCHAR(500) NULL,
    reviewed_at DATETIME2 NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_unavailability_requests_salonist
        FOREIGN KEY (salonist_id) REFERENCES salonists(id),

    CONSTRAINT CK_unavailability_requests_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),

    CONSTRAINT CK_unavailability_requests_time
        CHECK (end_at > start_at)
);

CREATE INDEX IX_unavailability_salonist_status
    ON unavailability_requests (salonist_id, status, start_at);
