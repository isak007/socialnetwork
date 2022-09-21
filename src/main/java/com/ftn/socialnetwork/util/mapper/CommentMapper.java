package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.Comment;
import com.ftn.socialnetwork.model.dto.CommentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, PostMapper.class})
public interface CommentMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "postId", source = "post.id")
    CommentDTO toDto(Comment comment);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "post.id", source = "postId")
    Comment toEntity(CommentDTO commentDTO);
}
