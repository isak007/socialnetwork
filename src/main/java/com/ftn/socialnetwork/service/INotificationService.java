package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.Notification;
import com.ftn.socialnetwork.model.dto.NotificationDTO;
import org.springframework.data.domain.Page;

public interface INotificationService {

    Notification findBySenderIdAndReceiverIdAndObjectIdAndActivityType(String token, Long senderId, Long receiverId,
                                                                       Long objectId, String activityType);

    Page<Notification> findAllForUser(String token, int page);

    Notification save(String token, NotificationDTO notificationDTO);

    Notification update(String token, NotificationDTO notificationDTO);

}
