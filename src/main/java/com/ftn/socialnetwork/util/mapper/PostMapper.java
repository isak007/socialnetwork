package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.Post;
import com.ftn.socialnetwork.model.dto.PostDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {Post.class})
public interface PostMapper {

    PostDTO toDto(Post post);

    Post toEntity(PostDTO postDTO);
}
