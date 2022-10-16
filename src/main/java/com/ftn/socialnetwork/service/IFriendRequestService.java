package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.FriendRequest;
import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.FriendRequestDTO;

import java.util.List;

public interface IFriendRequestService {

    FriendRequest findOne(Long id);

    List<FriendRequest> findAllForUser(String token);

    List<User> findFriendsForUser(String token, Long userId, int page);

    FriendRequest save(String token, FriendRequestDTO friendRequestDTO);

    FriendRequest update(String token, FriendRequestDTO friendRequestDTO);

    void delete(String token, Long id);
}
