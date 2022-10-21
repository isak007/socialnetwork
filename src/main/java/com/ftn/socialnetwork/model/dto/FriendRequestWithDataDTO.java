package com.ftn.socialnetwork.model.dto;

import lombok.Data;

@Data
public class FriendRequestWithDataDTO {

    private FriendRequestDTO friendRequestDTO;

    private UserDTO userDTO;

}
