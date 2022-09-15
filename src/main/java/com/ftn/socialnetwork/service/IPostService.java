package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.Post;

import java.util.List;

public interface IPostService {

    Post findOne(Long id);

    List<Post> findAll();

    Post save(Post post);

    Post update(Post post);

    void delete(Long id);
}
