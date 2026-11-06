package com.collo.dreadlocks.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<StudioNotification, Integer> {

    List<StudioNotification> findByAudienceAndSalonistIdIsNullOrderByCreatedAtDesc(NotificationAudience audience);

    List<StudioNotification> findByAudienceAndSalonistIdOrderByCreatedAtDesc(
            NotificationAudience audience,
            Integer salonistId
    );

    Optional<StudioNotification> findByIdAndAudienceAndSalonistIdIsNull(Integer id, NotificationAudience audience);

    List<StudioNotification> findBySalonistId(Integer salonistId);

    List<StudioNotification> findByAudienceAndCustomerIdOrderByCreatedAtDesc(
            NotificationAudience audience,
            Integer customerId
    );

    Optional<StudioNotification> findByIdAndAudienceAndCustomerId(
            Integer id,
            NotificationAudience audience,
            Integer customerId
    );

    Optional<StudioNotification> findByIdAndAudienceAndSalonistId(
            Integer id,
            NotificationAudience audience,
            Integer salonistId
    );
}
