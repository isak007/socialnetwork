package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.CreateEditUserDTO;
import com.ftn.socialnetwork.model.dto.LoginDTO;
import com.ftn.socialnetwork.model.dto.PasswordResetDTO;

import java.util.List;

public interface IUserService {

    String login(String usage, LoginDTO loginDTO);

    User accountActivation(String jwt);

    void sendEmailVerificationCode(String token, String email);

    void prePasswordResetAuth(String jwt);

    User passwordReset(PasswordResetDTO passwordResetDTO);

    void sendPasswordResetCode(String email);

    List<User> findUsers(String searchTerm, int page, int itemsPerPage);

    User findOne(Long id);

    List<User> findAll();

    User getUserData(String token, Long id);

    User getUserData(String token, String username);

    void save(CreateEditUserDTO createEditUserDTO);

    User update(String token, CreateEditUserDTO createEditUserDTO);

    byte[] getProfilePicture(Long userId);

    byte[] getPostPicture(String token, Long postId);

    void delete(Long id);

}
