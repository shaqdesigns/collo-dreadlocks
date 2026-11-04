CREATE TABLE customers (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    phone NVARCHAR(30) NOT NULL,
    email NVARCHAR(150) NULL,
    date_of_birth DATE NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT UQ_customers_phone UNIQUE (phone),

    CONSTRAINT CK_customers_name
        CHECK (LEN(LTRIM(RTRIM(name))) > 0),

    CONSTRAINT CK_customers_phone
        CHECK (LEN(LTRIM(RTRIM(phone))) > 0)
);

INSERT INTO customers (name, phone, email, date_of_birth, is_active)
VALUES
(
    'Amina Nyaboke',
    '0711000001',
    'amina@example.com',
    '1995-11-05',
    1
),
(
    'Brian Momanyi',
    '0711000002',
    'brian@example.com',
    '1990-06-18',
    1
),
(
    'Faith Kwamboka',
    '0711000003',
    NULL,
    NULL,
    1
);
