package com.ftn.socialnetwork.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class CommentWithDataDTO {

    private CommentDTO commentDTO;

    private List<CommentLikeDTO> commentLikesDTO;

    private boolean liked;
}
