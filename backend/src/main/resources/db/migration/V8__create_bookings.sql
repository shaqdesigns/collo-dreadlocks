CREATE TABLE bookings (
    id INT IDENTITY(1,1) PRIMARY KEY,
    reference UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    customer_id INT NOT NULL,
    service_id INT NOT NULL,
    salonist_id INT NOT NULL,
    start_at DATETIME2 NOT NULL,
    end_at DATETIME2 NOT NULL,
    status NVARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    duration_minutes INT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT UQ_bookings_reference UNIQUE (reference),

    CONSTRAINT FK_bookings_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id),

    CONSTRAINT FK_bookings_service
        FOREIGN KEY (service_id) REFERENCES services(id),

    CONSTRAINT FK_bookings_salonist
        FOREIGN KEY (salonist_id) REFERENCES salonists(id),

    CONSTRAINT CK_bookings_status
        CHECK (status IN (
            'PENDING',
            'CONFIRMED',
            'COMPLETED',
            'CANCELLED',
            'NO_SHOW'
        )),

    CONSTRAINT CK_bookings_time
        CHECK (end_at > start_at),

    CONSTRAINT CK_bookings_duration
        CHECK (duration_minutes > 0),

    CONSTRAINT CK_bookings_price
        CHECK (price >= 0)
);

CREATE INDEX IX_bookings_salonist_start
    ON bookings (salonist_id, start_at);

CREATE INDEX IX_bookings_customer_start
    ON bookings (customer_id, start_at);

CREATE INDEX IX_bookings_status_start
    ON bookings (status, start_at);
