package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.PostLike;
import com.ftn.socialnetwork.model.dto.PostLikeDTO;

import java.util.List;

public interface IPostLikeService {

    PostLike findOne(Long id);

    List<PostLike> findAllForPost(String token, Long postId);

    PostLike save(String token, PostLikeDTO postLikeDTO);

    void delete(String token, PostLikeDTO postLikeDTO);
}
