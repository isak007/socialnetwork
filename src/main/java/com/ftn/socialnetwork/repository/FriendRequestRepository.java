package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.FriendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findBySenderIdAndReceiverIdAndRequestStatus(Long senderId, Long receiverId, String requestStatus);

    List<FriendRequest> findByReceiverIdAndRequestStatus(Long receiverId, String requestStatus);

    Page<FriendRequest> findByReceiverIdAndRequestStatus(Long receiverId, String requestStatus, Pageable pageable);

    @Query("SELECT fr FROM FriendRequest fr WHERE fr.receiver.id = ?1 AND (fr.requestStatus LIKE ?2 OR fr.requestStatus LIKE ?3)")
    Page<FriendRequest> findByReceiverIdAndRequestStatusAcceptedDeclined(Long receiverId, String requestStatus , String requestStatus1, Pageable pageable);

    List<FriendRequest> findBySenderIdAndRequestStatus(Long senderId, String requestStatus);

    @Query("SELECT fr FROM FriendRequest fr WHERE ((fr.sender.id = ?1 AND fr.receiver.id = ?2) OR (fr.sender.id = ?2 AND fr.receiver.id = ?1))" +
            "AND (fr.requestStatus LIKE ?3 OR fr.requestStatus LIKE ?4)")
    Optional<FriendRequest> checkIfFriendRequestExists(Long user1Id, Long user2Id, String requestStatus , String requestStatus1);


}
