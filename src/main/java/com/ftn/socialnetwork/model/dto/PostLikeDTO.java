package com.ftn.socialnetwork.model.dto;

import lombok.Data;

@Data
public class PostLikeDTO {

    private Long id;

    private Long postId;

    private Long userId;

}
