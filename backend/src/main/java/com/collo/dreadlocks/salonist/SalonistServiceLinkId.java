package com.collo.dreadlocks.salonist;

import java.io.Serializable;
import java.util.Objects;

public class SalonistServiceLinkId implements Serializable {

    private Integer salonistId;
    private Integer serviceId;

    public SalonistServiceLinkId() {
    }

    public SalonistServiceLinkId(Integer salonistId, Integer serviceId) {
        this.salonistId = salonistId;
        this.serviceId = serviceId;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SalonistServiceLinkId that)) return false;
        return Objects.equals(salonistId, that.salonistId)
                && Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salonistId, serviceId);
    }
}
