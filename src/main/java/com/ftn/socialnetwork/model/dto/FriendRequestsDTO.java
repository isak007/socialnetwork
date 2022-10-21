package com.ftn.socialnetwork.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class FriendRequestsDTO {

    private int totalFriendRequests;

    private List<FriendRequestWithDataDTO> friendRequestsWithDataDTO;

}
