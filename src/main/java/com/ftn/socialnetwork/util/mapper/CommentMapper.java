package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.Comment;
import com.ftn.socialnetwork.model.dto.CommentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, PostMapper.class})
public interface CommentMapper {

    @Mapping(target = "userDTO", source = "user")
    @Mapping(target = "postDTO", source = "post")
    CommentDTO toDto(Comment comment);

    @Mapping(target = "user", source = "userDTO")
    @Mapping(target = "post", source = "postDTO")
    Comment toEntity(CommentDTO commentDTO);
}
