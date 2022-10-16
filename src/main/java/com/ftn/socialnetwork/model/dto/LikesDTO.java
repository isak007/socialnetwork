package com.ftn.socialnetwork.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class LikesDTO {

    private List<UserDTO> users;

    private int totalLikes;

}
