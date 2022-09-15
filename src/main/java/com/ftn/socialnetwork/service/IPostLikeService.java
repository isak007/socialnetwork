package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.PostLike;

import java.util.List;

public interface IPostLikeService {

    PostLike findOne(Long id);

    List<PostLike> findAll();

    PostLike save(PostLike postLike);

    PostLike update(PostLike postLike);

    void delete(Long id);
}
