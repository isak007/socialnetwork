package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.PostLike;
import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.PostLikeDTO;
import org.springframework.data.domain.Page;

public interface IPostLikeService {

    PostLike findOne(Long id);

    Page<User> findAllForPost(String token, Long postId, int page);

    User save(String token, PostLikeDTO postLikeDTO);

    void delete(String token, PostLikeDTO postLikeDTO);
}
