CREATE TABLE service_photos (
    id INT IDENTITY(1,1) PRIMARY KEY,
    service_id INT NOT NULL,
    image_url NVARCHAR(500) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_service_photos_service
        FOREIGN KEY (service_id) REFERENCES services(id)
);

CREATE INDEX IX_service_photos_service
    ON service_photos (service_id, created_at);

CREATE TABLE notifications (
    id INT IDENTITY(1,1) PRIMARY KEY,
    audience NVARCHAR(20) NOT NULL,
    salonist_id INT NULL,
    booking_id INT NULL,
    message NVARCHAR(500) NOT NULL,
    read_at DATETIME2 NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT CK_notifications_audience
        CHECK (audience IN ('ADMIN', 'SALONIST')),

    CONSTRAINT FK_notifications_salonist
        FOREIGN KEY (salonist_id) REFERENCES salonists(id),

    CONSTRAINT FK_notifications_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE INDEX IX_notifications_audience
    ON notifications (audience, salonist_id, created_at);

CREATE TABLE manual_works (
    id INT IDENTITY(1,1) PRIMARY KEY,
    salonist_id INT NOT NULL,
    service_id INT NOT NULL,
    customer_name NVARCHAR(150) NULL,
    price DECIMAL(10,2) NOT NULL,
    worked_at DATETIME2 NOT NULL,
    note NVARCHAR(500) NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_manual_works_salonist
        FOREIGN KEY (salonist_id) REFERENCES salonists(id),

    CONSTRAINT FK_manual_works_service
        FOREIGN KEY (service_id) REFERENCES services(id),

    CONSTRAINT CK_manual_works_price
        CHECK (price > 0)
);

CREATE INDEX IX_manual_works_salonist
    ON manual_works (salonist_id, worked_at);
