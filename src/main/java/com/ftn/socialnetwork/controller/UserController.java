package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.RegistrationDTO;
import com.ftn.socialnetwork.model.dto.UserDTO;
import com.ftn.socialnetwork.model.dto.JwtDTO;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.model.dto.LoginDTO;
import com.ftn.socialnetwork.service.IUserService;
import com.ftn.socialnetwork.util.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping(value = "users")
public class UserController {

    private final IUserService userService;
    private final UserMapper userMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;

    public UserController(IUserService userService, UserMapper userMapper,
                          JwtTokenUtil jwtTokenUtil, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
    }


    @PostMapping(value = "login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        String jwt = jwtTokenUtil.generateAccessToken(user);
        JwtDTO jwtResponse = new JwtDTO(jwt);

        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping(value = "registration")
    public ResponseEntity<?> registration(@RequestBody RegistrationDTO registrationDTO) {
        UserDTO userDTO = userMapper.toDto(userService.save(registrationDTO));

        return ResponseEntity.ok(userDTO);
    }

    @GetMapping(value = "{id}")
    public ResponseEntity<UserDTO> getUserData(HttpServletRequest request, @PathVariable Long id) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<UserDTO>(userMapper.toDto(userService.getUserData(token,id)), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UserDTO> editPersonalData(HttpServletRequest request, @RequestBody UserDTO userDTO) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<UserDTO>(userMapper.toDto(userService.update(token,userDTO)), HttpStatus.OK);
    }

}
