package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.Post;
import com.ftn.socialnetwork.model.PostWithData;
import com.ftn.socialnetwork.model.dto.PostDTO;

import java.util.List;

public interface IPostService {

    Post findOne(Long id);

    List<PostWithData> findAllForMainPage(String token);

    List<PostWithData> findAllForUser(String token, Long userId);

    Post save(String token, PostDTO postDTO);

    Post update(String token, PostDTO postDTO);

    void delete(String token, Long id);
}
