CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(150) NOT NULL,
    password_hash NVARCHAR(100) NOT NULL,
    role NVARCHAR(20) NOT NULL,
    salonist_id INT NULL,
    customer_id INT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT UQ_users_email UNIQUE (email),

    CONSTRAINT FK_users_salonist
        FOREIGN KEY (salonist_id) REFERENCES salonists(id),

    CONSTRAINT FK_users_customer
        FOREIGN KEY (customer_id) REFERENCES customers(id),

    CONSTRAINT CK_users_role
        CHECK (role IN ('ADMIN', 'SALONIST', 'CUSTOMER')),

    CONSTRAINT CK_users_email
        CHECK (LEN(LTRIM(RTRIM(email))) > 0),

    CONSTRAINT CK_users_password_hash
        CHECK (LEN(password_hash) > 0)
);

CREATE UNIQUE INDEX UQ_users_salonist
    ON users (salonist_id)
    WHERE salonist_id IS NOT NULL;

CREATE UNIQUE INDEX UQ_users_customer
    ON users (customer_id)
    WHERE customer_id IS NOT NULL;
