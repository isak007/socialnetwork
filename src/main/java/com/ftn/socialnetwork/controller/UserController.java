package com.ftn.socialnetwork.controller;

import com.ftn.socialnetwork.model.dto.*;
import com.ftn.socialnetwork.service.IUserService;
import com.ftn.socialnetwork.util.RestService;
import com.ftn.socialnetwork.util.mapper.UserMapper;
import com.ftn.socialnetwork.util.validators.OnCreate;
import com.ftn.socialnetwork.util.validators.OnUpdate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.io.*;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping(value = "users")
@CrossOrigin("http://localhost:8081/")
public class UserController {

    private final IUserService userService;
    private final UserMapper userMapper;
    private final RestService restService;

    public UserController(IUserService userService, UserMapper userMapper, RestService restService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.restService = restService;
    }


    @GetMapping(value = "search/{searchTerm}")
    public ResponseEntity<List<UserDTO>> findUsers(@PathVariable String searchTerm,
                                                   @RequestParam("page") Integer page ,
                                                   @RequestParam("itemsPerPage") Integer itemsPerPage) {
        // for testing purposes
//        try {
//            Thread.sleep(10 * 1000);
//        } catch (InterruptedException ie) {
//            Thread.currentThread().interrupt();
//        }
        return new ResponseEntity<List<UserDTO>>(userService.findUsers(searchTerm,page,itemsPerPage).stream().map(userMapper::toDto).collect(Collectors.toList()), HttpStatus.OK);
    }


    @PostMapping(value = "login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        JwtDTO jwtResponse = new JwtDTO(userService.login("login",loginDTO));

        return ResponseEntity.ok(jwtResponse);
    }

    @GetMapping(value = "account-activation")
    public ResponseEntity<?> accountActivation(@RequestParam("jwt") String jwt) {
        return ResponseEntity.ok(userMapper.toDto(userService.accountActivation(jwt)));
    }

    @GetMapping(value = "send-email-verification-code")
    public ResponseEntity<?> sendEmailVerificationCode(HttpServletRequest request, @RequestParam("email") String email) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);
        userService.sendEmailVerificationCode(token, email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "password-reset")
    public ResponseEntity<?> passwordReset(@Valid @RequestBody PasswordResetDTO passwordResetDTO) {
        return ResponseEntity.ok(userMapper.toDto(userService.passwordReset(passwordResetDTO)));
    }

    @GetMapping(value = "pre-password-reset-auth")
    public ResponseEntity<?> prePasswordResetAuth(@RequestParam("jwt") String jwt) {
        userService.prePasswordResetAuth(jwt);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "send-password-reset-code")
    public ResponseEntity<?> sendPasswordResetCode(@RequestParam("email") String email) {
        userService.sendPasswordResetCode(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/profile-picture",
            produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getProfilePicture(@RequestParam Long userId) throws IOException {

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(userService.getProfilePicture(userId));
    }

    @GetMapping(value = "/post-picture",
            produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getPostPicture(HttpServletRequest request, @RequestParam Long postId) throws IOException {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(userService.getPostPicture(token,postId));
    }


    @Validated(OnCreate.class)
    @PostMapping(value = "registration")
    public ResponseEntity<?> registration(@Valid @RequestBody CreateEditUserDTO createEditUserDTO) throws ConstraintViolationException{
        userService.save(createEditUserDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "fetch/{id}")
    public ResponseEntity<UserDTO> getUserData(HttpServletRequest request, @PathVariable Long id) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<UserDTO>(userMapper.toDto(userService.getUserData(token,id)), HttpStatus.OK);
    }

    @GetMapping(value = "places-token")
    public ResponseEntity<PlacesTokenDTO> getPlacesToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        try {
            String newToken = this.restService.renewValidationTokenWebClient();
            return new ResponseEntity<PlacesTokenDTO>(new PlacesTokenDTO(newToken), HttpStatus.OK);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "city-list")
    public ResponseEntity<Object> getCityList(HttpServletRequest request, @PathParam("queryString") String queryString) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return this.restService.getCityList(queryString);
//            return new ResponseEntity<PlacesTokenDTO>(new PlacesTokenDTO(newToken), HttpStatus.OK);
    }

    @GetMapping(value = "fetch/username/{username}")
    public ResponseEntity<UserDTO> getUserData(HttpServletRequest request, @PathVariable String username) {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<UserDTO>(userMapper.toDto(userService.getUserData(token,username)), HttpStatus.OK);
    }

    @Validated(OnUpdate.class)
    @PutMapping
    public ResponseEntity<UserDTO> editPersonalData(HttpServletRequest request, @Valid @RequestBody CreateEditUserDTO createEditUserDTO) throws ConstraintViolationException {
        String header = request.getHeader("Authorization");
        String token = header.substring(7);

        return new ResponseEntity<UserDTO>(userMapper.toDto(userService.update(token, createEditUserDTO)), HttpStatus.OK);
    }


}
