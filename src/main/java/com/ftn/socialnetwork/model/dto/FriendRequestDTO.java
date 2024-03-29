package com.ftn.socialnetwork.model.dto;

import lombok.Data;

@Data
public class FriendRequestDTO {

    private Long id;

    private String requestStatus;

    private Long senderId;

    private Long receiverId;

    private String dateCreated;

}
