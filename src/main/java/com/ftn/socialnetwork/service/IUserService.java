package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.RegistrationDTO;
import com.ftn.socialnetwork.model.dto.UserDTO;

import java.util.List;

public interface IUserService {

    List<User> findUsers(String searchTerm);

    User findOne(Long id);

    List<User> findAll();

    User getUserData(String token, Long id);

    User save(RegistrationDTO registrationDTO);

    User update(String token, UserDTO userDTO);

    void delete(Long id);

}
