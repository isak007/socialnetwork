package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.PostLike;
import com.ftn.socialnetwork.repository.PostLikeRepository;
import com.ftn.socialnetwork.service.IPostLikeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PostLikeService implements IPostLikeService {

    private final PostLikeRepository postLikeRepository;

    public PostLikeService(PostLikeRepository postLikeRepository) {
        this.postLikeRepository = postLikeRepository;
    }

    @Override
    public PostLike findOne(Long id) {
        Optional<PostLike> postLike = postLikeRepository.findById(id);
        if(postLike.isEmpty()) {
            throw new NoSuchElementException("Post like with id = " + id + " not found!");
        }
        return postLike.get();
    }

    @Override
    public List<PostLike> findAll() {
        List<PostLike> postLikes = postLikeRepository.findAll();
        return postLikes;
    }

    @Override
    public PostLike save(PostLike postLike) {
        return postLikeRepository.save(postLike);
    }

    @Override
    public PostLike update(PostLike postLike) {
        return postLikeRepository.save(postLike);
    }

    @Override
    public void delete(Long id) {
        postLikeRepository.deleteById(id);
    }
}
