CREATE TABLE payments (
    id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    method NVARCHAR(10) NOT NULL,
    status NVARCHAR(20) NOT NULL,
    phone NVARCHAR(20) NULL,
    amount DECIMAL(10,2) NOT NULL,
    checkout_request_id NVARCHAR(100) NULL,
    merchant_request_id NVARCHAR(100) NULL,
    mpesa_receipt NVARCHAR(40) NULL,
    result_description NVARCHAR(255) NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    approved_at DATETIME2 NULL,

    CONSTRAINT UQ_payments_booking UNIQUE (booking_id),

    CONSTRAINT FK_payments_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id),

    CONSTRAINT CK_payments_method
        CHECK (method IN ('MPESA', 'CASH')),

    CONSTRAINT CK_payments_status
        CHECK (status IN ('AWAITING', 'PAID', 'APPROVED', 'FAILED')),

    CONSTRAINT CK_payments_amount
        CHECK (amount >= 0)
);

CREATE UNIQUE INDEX UQ_payments_checkout
    ON payments (checkout_request_id)
    WHERE checkout_request_id IS NOT NULL;

CREATE INDEX IX_payments_status
    ON payments (status, created_at);
