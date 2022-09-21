package com.ftn.socialnetwork.util.mapper;

import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.UserDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {User.class})
public interface UserMapper {

    UserDTO toDto(User user);

    User toEntity(UserDTO userDTO);
}
