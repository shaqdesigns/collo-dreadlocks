package com.collo.dreadlocks.salonist;

import jakarta.persistence.*;

@Entity
@Table(name = "salonist_services")
@IdClass(SalonistServiceLinkId.class)
public class SalonistServiceLink {

    @Id
    @Column(name = "salonist_id")
    private Integer salonistId;

    @Id
    @Column(name = "service_id")
    private Integer serviceId;

    public Integer getSalonistId() {
        return salonistId;
    }

    public void setSalonistId(Integer salonistId) {
        this.salonistId = salonistId;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }
}
