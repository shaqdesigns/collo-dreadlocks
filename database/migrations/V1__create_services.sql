CREATE TABLE services (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    slug NVARCHAR(160) NOT NULL,
    description NVARCHAR(1000) NULL,
    price DECIMAL(10,2) NOT NULL,
    duration_minutes INT NOT NULL,
    image_url NVARCHAR(500) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT UQ_services_slug UNIQUE (slug),
    CONSTRAINT CK_services_price CHECK (price >= 0),
    CONSTRAINT CK_services_duration CHECK (duration_minutes > 0)
);
