package com.ftn.socialnetwork.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class PostsDTO {

    private List<PostWithDataDTO> postsWithDataDTO;

    private int totalPosts;

}
