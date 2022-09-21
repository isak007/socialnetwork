package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.Post;
import com.ftn.socialnetwork.model.dto.PostDTO;

import java.util.List;

public interface IPostService {

    Post findOne(Long id);

    List<Post> findAllForMainPage(String token);

    List<Post> findAllForUser(String token, Long userId);

    Post save(String token, PostDTO postDTO);

    Post update(String token, PostDTO postDTO);

    void delete(String token, Long id);
}
