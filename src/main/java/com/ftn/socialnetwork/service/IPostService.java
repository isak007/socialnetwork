package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.Post;
import com.ftn.socialnetwork.model.PostWithData;
import com.ftn.socialnetwork.model.dto.PostDTO;

import java.util.List;

public interface IPostService {

    Post findOne(Long id);

    List<PostWithData> findAllForMainPage(String token, int page);

    List<PostWithData> findAllForUser(String token, Long userId, int page);

    PostWithData save(String token, PostDTO postDTO);

    Post update(String token, PostDTO postDTO);

    void delete(String token, Long id);
}
