package com.ftn.socialnetwork.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostWithDataDTO {

    private PostDTO postDTO;

    private List<CommentDTO> commentsDTO;

    private List<PostLikeDTO> postLikesDTO;

    private boolean liked;
}
