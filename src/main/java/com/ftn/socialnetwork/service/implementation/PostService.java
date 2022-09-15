package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.Post;
import com.ftn.socialnetwork.repository.PostRepository;
import com.ftn.socialnetwork.service.IPostService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PostService implements IPostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }


    @Override
    public Post findOne(Long id) {
        Optional<Post> post = postRepository.findById(id);
        if(post.isEmpty()) {
            throw new NoSuchElementException("Post with id = " + id + " not found!");
        }
        return post.get();
    }

    @Override
    public List<Post> findAll() {
        List<Post> posts = postRepository.findAll();
        return posts;
    }

    @Override
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Override
    public Post update(Post post) {
        return postRepository.save(post);
    }

    @Override
    public void delete(Long id) {
        postRepository.deleteById(id);
    }
}
