package com.ftn.socialnetwork.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class CommentWithDataDTO {

    private CommentDTO commentDTO;

    private List<UserDTO> commentLikesDTO;

    private int totalLikes;

    private boolean liked;
}
