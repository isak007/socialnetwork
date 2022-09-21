package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findBySenderIdAndReceiverIdAndRequestStatus(Long senderId, Long receiverId, String requestStatus);

    List<FriendRequest> findByReceiverIdAndRequestStatus(Long receiverId, String requestStatus);

    List<FriendRequest> findBySenderIdAndRequestStatus(Long senderId, String requestStatus);

}
