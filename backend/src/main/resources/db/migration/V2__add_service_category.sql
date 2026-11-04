ALTER TABLE services
ADD category NVARCHAR(50) NOT NULL
    CONSTRAINT DF_services_category DEFAULT 'OTHER';
