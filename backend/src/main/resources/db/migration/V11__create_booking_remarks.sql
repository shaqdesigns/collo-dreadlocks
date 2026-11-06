CREATE TABLE booking_remarks (
    id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    customer_id INT NOT NULL,
    salonist_id INT NOT NULL,
    body NVARCHAR(1000) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT UQ_booking_remarks_booking UNIQUE (booking_id),

    CONSTRAINT FK_booking_remarks_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id),

    CONSTRAINT FK_booking_remarks_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id),

    CONSTRAINT FK_booking_remarks_salonist
        FOREIGN KEY (salonist_id) REFERENCES salonists(id),

    CONSTRAINT CK_booking_remarks_body
        CHECK (LEN(LTRIM(RTRIM(body))) > 0)
);

CREATE INDEX IX_booking_remarks_salonist
    ON booking_remarks (salonist_id, created_at);
