package com.ftn.socialnetwork.repository;

import com.ftn.socialnetwork.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
}
