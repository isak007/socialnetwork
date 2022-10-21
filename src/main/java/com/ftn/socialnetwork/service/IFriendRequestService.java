package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.FriendRequest;
import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.FriendRequestDTO;
import com.ftn.socialnetwork.model.dto.FriendRequestsDTO;
import java.util.List;

public interface IFriendRequestService {

    FriendRequest findOne(Long id);

    FriendRequest checkIfFriendRequestExists(String token, Long user1Id, Long user2Id);

    FriendRequestsDTO findAllForUser(String token, int page);

    FriendRequestsDTO findAllNonPendingForUser(String token, int page);

    List<User> findFriendsForUser(String token, Long userId, int page);

    FriendRequest save(String token, FriendRequestDTO friendRequestDTO);

    FriendRequest update(String token, FriendRequestDTO friendRequestDTO);

    void delete(String token, Long id);
}
