CREATE TABLE salonist_availability (
    id INT IDENTITY(1,1) PRIMARY KEY,
    salonist_id INT NOT NULL,
    day_of_week NVARCHAR(10) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT FK_salonist_availability_salonist
        FOREIGN KEY (salonist_id)
        REFERENCES salonists(id),

    CONSTRAINT CK_salonist_availability_day
        CHECK (day_of_week IN (
            'MONDAY',
            'TUESDAY',
            'WEDNESDAY',
            'THURSDAY',
            'FRIDAY',
            'SATURDAY',
            'SUNDAY'
        )),

    CONSTRAINT CK_salonist_availability_time
        CHECK (end_time > start_time)
);

CREATE INDEX IX_salonist_availability_salonist_day
    ON salonist_availability (salonist_id, day_of_week);

INSERT INTO salonist_availability (
    salonist_id,
    day_of_week,
    start_time,
    end_time,
    is_active
)
SELECT
    s.id,
    d.day_of_week,
    CAST('09:00:00' AS TIME),
    CAST('17:00:00' AS TIME),
    1
FROM salonists s
CROSS JOIN (
    VALUES
        ('MONDAY'),
        ('TUESDAY'),
        ('WEDNESDAY'),
        ('THURSDAY'),
        ('FRIDAY'),
        ('SATURDAY')
) AS d(day_of_week)
WHERE s.is_active = 1;
