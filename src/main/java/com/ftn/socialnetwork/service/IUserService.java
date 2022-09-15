package com.ftn.socialnetwork.service;

import com.ftn.socialnetwork.model.User;

import java.util.List;

public interface IUserService {

    User findOne(Long id);

    List<User> findAll();

    User save(User user);

    User update(User user);

    void delete(Long id);

}
