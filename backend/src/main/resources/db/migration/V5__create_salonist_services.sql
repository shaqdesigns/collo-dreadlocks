CREATE TABLE salonist_services (
    salonist_id INT NOT NULL,
    service_id INT NOT NULL,

    CONSTRAINT PK_salonist_services
        PRIMARY KEY (salonist_id, service_id),

    CONSTRAINT FK_salonist_services_salonist
        FOREIGN KEY (salonist_id)
        REFERENCES salonists(id),

    CONSTRAINT FK_salonist_services_service
        FOREIGN KEY (service_id)
        REFERENCES services(id)
);
