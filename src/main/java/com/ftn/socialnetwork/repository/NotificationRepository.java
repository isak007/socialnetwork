package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Notification findBySenderIdAndReceiverIdAndObjectIdAndActivityType(Long senderId, Long receiverId, Long objectId, String activityType);
    List<Notification> findAllByReceiverIdAndObjectTypeNot(Long receiverId, String objectType);

}
