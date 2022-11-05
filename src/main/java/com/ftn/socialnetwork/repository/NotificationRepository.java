package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

//    Optional<Chat> findById(Long id);

    Notification findBySenderIdAndReceiverIdAndObjectId(Long senderId, Long receiverId, Long objectId);
    List<Notification> findAllByReceiverIdAndObjectTypeNot(Long receiverId, String objectType);
//
//    @Query("SELECT c FROM Chat c WHERE (c.user1.id = ?1 AND c.user2.id = ?2) OR (c.user1.id = ?2 AND c.user2.id = ?1)")
//    Chat findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

}
