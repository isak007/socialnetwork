package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.Post;
import com.ftn.socialnetwork.model.PostWithData;
import com.ftn.socialnetwork.model.dto.PostDTO;
import org.springframework.data.domain.Page;
public interface IPostService {

    PostWithData findOne(String token, Long id);

    Page<PostWithData> findAllForMainPage(String token, int page);

    Page<PostWithData> findAllForUser(String token, Long userId, int page);

    PostWithData save(String token, PostDTO postDTO);

    Post update(String token, PostDTO postDTO);

    void delete(String token, Long id);
}
