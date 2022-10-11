package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.CreateEditUserDTO;
import com.ftn.socialnetwork.model.dto.LoginDTO;

import java.util.List;

public interface IUserService {

    User accountActivation(String jwt);

    String login(String usage, LoginDTO loginDTO);

    void sendEmailVerificationCode(String token, String email);

    List<User> findUsers(String searchTerm);

    User findOne(Long id);

    List<User> findAll();

    User getUserData(String token, Long id);

    void save(CreateEditUserDTO createEditUserDTO);

    User update(String token, CreateEditUserDTO createEditUserDTO);

    byte[] getProfilePicture(Long userId);

    byte[] getPostPicture(String token, Long postId);

    void delete(Long id);

}
