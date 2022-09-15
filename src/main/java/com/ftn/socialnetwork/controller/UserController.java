package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.UserDTO;
import com.ftn.socialnetwork.service.IUserService;
import com.ftn.socialnetwork.util.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "users")
public class UserController {

    private final IUserService userService;
    private final UserMapper userMapper;

    public UserController(IUserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }


    @GetMapping(value = "{id}")
    public ResponseEntity<UserDTO> getPersonalData(@PathVariable Long id) {
        User user  = userService.findOne(id);
        if (user == null){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<UserDTO>(userMapper.toDto(user), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UserDTO> editPersonalData(@RequestBody UserDTO userDTO) {
        User user  = userService.findOne(userDTO.getId());
        if (user == null){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getPassword() != null && !userDTO.getPassword().equals("")) {
            String newPassword = new BCryptPasswordEncoder().encode(userDTO.getPassword());
            System.out.println("New raw password: "+ userDTO.getPassword());
            System.out.println("New encoded password: "+ newPassword);
            user.setPassword(newPassword);
        }

        return new ResponseEntity<UserDTO>(userMapper.toDto(userService.save(user)), HttpStatus.OK);
    }

}
