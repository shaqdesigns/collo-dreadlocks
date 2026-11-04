CREATE TABLE salonists (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    phone NVARCHAR(30) NOT NULL,
    email NVARCHAR(150) NULL,
    bio NVARCHAR(1000) NULL,
    profile_image_url NVARCHAR(500) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT CK_salonists_name
        CHECK (LEN(LTRIM(RTRIM(name))) > 0),

    CONSTRAINT CK_salonists_phone
        CHECK (LEN(LTRIM(RTRIM(phone))) > 0)
);
