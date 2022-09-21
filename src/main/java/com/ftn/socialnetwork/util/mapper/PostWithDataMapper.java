package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.PostWithData;
import com.ftn.socialnetwork.model.dto.PostWithDataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PostMapper.class, CommentMapper.class, PostLikeMapper.class})
public interface PostWithDataMapper {


    @Mapping(target = "postDTO", source = "post")
    @Mapping(target = "commentsDTO", source = "comments")
    @Mapping(target = "postLikesDTO", source = "postLikes")
    PostWithDataDTO toDto(PostWithData postWithData);

    @Mapping(target = "post", source = "postDTO")
    @Mapping(target = "comments", source = "commentsDTO")
    @Mapping(target = "postLikes", source = "postLikesDTO")
    PostWithData toEntity(PostWithDataDTO postWithDataDTO);
}
