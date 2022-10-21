package com.ftn.socialnetwork.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class CommentsDTO {

    private List<CommentWithDataDTO> commentsWithDataDTO;

    private int totalComments;
}
