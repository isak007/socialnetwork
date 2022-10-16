package com.ftn.socialnetwork.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostWithDataDTO {

    private PostDTO postDTO;

    private List<UserDTO> postLikesDTO;

    private int totalLikes;

    private boolean liked;
}
