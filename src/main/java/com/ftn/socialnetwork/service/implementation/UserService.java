package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.Post;
import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.CreateEditUserDTO;
import com.ftn.socialnetwork.model.dto.LoginDTO;
import com.ftn.socialnetwork.model.dto.PasswordResetDTO;
import com.ftn.socialnetwork.repository.FriendRequestRepository;
import com.ftn.socialnetwork.repository.PostRepository;
import com.ftn.socialnetwork.repository.UserRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.IUserService;
import com.ftn.socialnetwork.util.exception.*;
import com.ftn.socialnetwork.util.mail.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static java.lang.String.format;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final FriendRequestRepository friendRequestRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final String PROFILE_TYPE = "profile";
    private final String POST_TYPE = "post";
    private final String PHOTOS_PATH = "src/main/resources/user-photos/";

    public UserService(UserRepository userRepository, PostRepository postRepository, JwtTokenUtil jwtTokenUtil, FriendRequestRepository friendRequestRepository,
                       EmailService emailService, AuthenticationManager authenticationManager, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.friendRequestRepository = friendRequestRepository;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    @Override
    public User accountActivation(String jwt) {
        if (!jwtTokenUtil.validate(jwt)){
            throw new UnauthorizedException("Invalid activation token.");
        }
        String username = jwtTokenUtil.getUsername(jwt);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException("User not found.");
        }
        User user = userOpt.get();
        if (user.isActivated()) {
            throw new UnauthorizedException("This account is already activated.");
        }
        user.setActivated(true);
        return userRepository.save(user);
    }

    @Override
    public String login(String usage,LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );
        User user = (User) authentication.getPrincipal();
        if (usage.equals("login") && !user.isActivated()){
            throw new UnauthorizedException("Bad credentials.");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwtTokenUtil.generateAccessToken(user);
    }

    @Override
    public User findOne(Long id) {
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()) {
            throw new NoSuchElementException("User with id = " + id + " not found!");
        }
        return user.get();
    }

    @Override
    public List<User> findUsers(String searchTerm, int page, int itemsPerPage) {
        Pageable pageable = PageRequest.of(page,itemsPerPage);
        Page<User> usersFound = userRepository.searchByFirstNameLastNameUsername(searchTerm,pageable);
        return usersFound.getContent();
    }


    public Boolean areFriends(Long user1id, Long user2id){
        return friendRequestRepository.findBySenderIdAndReceiverIdAndRequestStatus(user1id, user2id, "ACCEPTED").isPresent() ||
                friendRequestRepository.findBySenderIdAndReceiverIdAndRequestStatus(user2id, user1id, "ACCEPTED").isPresent();
    }


    @Override
    public User getUserData(String token, Long id) {
        Optional<User> user  = userRepository.findById(id);
        if (user.isEmpty()){
            throw new EntityNotFoundException(format("User with id '%s' not found.",id));
        }

        return user.get();
    }

    @Override
    public User getUserData(String token, String username) {

        Optional<User> user  = userRepository.findByUsername(username);
        if (user.isEmpty()){
            throw new EntityNotFoundException(format("User with username '%s' not found.",username));
        }
        return user.get();
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public String activationToken(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );
        User user = (User) authentication.getPrincipal();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwtTokenUtil.generateAccessToken(user);
    }

    @Override
    public void sendEmailVerificationCode(String token, String email){
        Optional<User> userOpt =userRepository.findById(jwtTokenUtil.getUserId(token));
        if (userOpt.isEmpty()){
            throw new UnauthorizedException("You are not authorized for this action.");
        }
        // check if email is taken (shouldn't happen as user will have to confirm his email)
        if (userRepository.findByEmail(email).isPresent()){
            throw new EmailExistsException(format("Email '%s' is already in use.", email));
        }

        User user = userOpt.get();
        // creating verification token using user's encoded password and email
        String secret = user.getPassword().substring(7,20) + email;
        String verificationCode = bCryptPasswordEncoder.encode(secret);

        emailService.sendMessage(email,
                "New email verification - Virtual Connect",
                "Verification code: "+ verificationCode);
    }



    @Override
    public void prePasswordResetAuth(String jwt) {
        if (!jwtTokenUtil.validate(jwt)){
            throw new UnauthorizedException("Invalid token.");
        }
        String email = jwtTokenUtil.getPasswordResetAttributes(jwt,"email");
        String resetCode = jwtTokenUtil.getPasswordResetAttributes(jwt,"resetCode");

        Optional<User> userOpt = userRepository.findByEmail(email);
        // check if email exists
        if (userOpt.isEmpty()){
            throw new EntityNotFoundException(format("Email '%s' does not exist.",email));
        }
        User user = userOpt.get();

        if (!bCryptPasswordEncoder.matches(user.getPassword() + email, resetCode)) {
            throw new UnauthorizedException("Password reset code is incorrect.");
        }
    }
    @Override
    public User passwordReset(PasswordResetDTO passwordResetDTO) {

        String jwt = passwordResetDTO.getPasswordResetJwt();

        if (!jwtTokenUtil.validate(jwt)){
            throw new UnauthorizedException("Invalid token.");
        }

        String email = jwtTokenUtil.getPasswordResetAttributes(jwt,"email");
        String resetCode = jwtTokenUtil.getPasswordResetAttributes(jwt,"resetCode");
        String newPassword = passwordResetDTO.getNewPassword();

        Optional<User> userOpt = userRepository.findByEmail(email);
        // check if email exists
        if (userOpt.isEmpty()){
            throw new EntityNotFoundException(format("Email '%s' does not exist.",email));
        }
        User user = userOpt.get();

        if (bCryptPasswordEncoder.matches(newPassword,user.getPassword())){
            throw new UnauthorizedException("Password is the same as old one.");
        }
        if (!bCryptPasswordEncoder.matches(user.getPassword() + email, resetCode)){
            throw new UnauthorizedException("Password reset code is incorrect.");
        }
        if (newPassword.length() < 6){
            throw new UnauthorizedException("New password size is too small.");
        }
        String password = new BCryptPasswordEncoder().encode(newPassword);
        user.setPassword(password);
        return userRepository.save(user);

    }

    @Override
    public void sendPasswordResetCode(String email){
        Optional<User> userOpt = userRepository.findByEmail(email);
        // check if email exists
        if (userOpt.isEmpty()){
            throw new EntityNotFoundException(format("Email '%s' does not exist.",email));
        }

        User user = userOpt.get();
        // creating verification token using user's encoded password and email
        String secret = user.getPassword() + email;
        String resetCode = bCryptPasswordEncoder.encode(secret);
        String passwordResetCode = jwtTokenUtil.generatePasswordResetToken(email,resetCode);
        emailService.sendMessage(email,
                "Password Reset - Virtual Connect",
                "Password reset link - http://localhost:8081/password-reset/"+passwordResetCode+
                        "\nThis link will expire in 5 minutes.");
    }

    @Override
    public void save(CreateEditUserDTO createEditUserDTO) {

        // check if username is taken
        if (userRepository.findByUsername(createEditUserDTO.getUsername()).isPresent()){
            throw new UsernameExistsException(format("Username '%s' is already in use.", createEditUserDTO.getUsername()));
        }

        if (userRepository.findByEmail(createEditUserDTO.getEmail()).isPresent()){
            throw new EmailExistsException(format("Email '%s' is already in use.", createEditUserDTO.getEmail()));
        }

        User user = new User();
        user.setUsername(createEditUserDTO.getUsername());
        String password = new BCryptPasswordEncoder().encode(createEditUserDTO.getPassword());
        user.setPassword(password);
        user.setEmail(createEditUserDTO.getEmail());
        user.setFirstName(createEditUserDTO.getFirstName());
        user.setLastName(createEditUserDTO.getLastName());
        user.setDateOfBirth(createEditUserDTO.getDateOfBirth());
        user.setCity(createEditUserDTO.getCity());

        if(createEditUserDTO.getProfilePicture() != null && !createEditUserDTO.getProfilePicture().equals("") && createEditUserDTO.getPictureBase64() != null) {
            user.setProfilePicture(createEditUserDTO.getProfilePicture());
        }
        user.setActivated(false);
        User userReturned = userRepository.save(user);

        if(createEditUserDTO.getProfilePicture() != null && !createEditUserDTO.getProfilePicture().equals("") && createEditUserDTO.getPictureBase64() != null) {
            this.uploadPicture(userReturned.getId(), createEditUserDTO.getProfilePicture(), createEditUserDTO.getPictureBase64(),this.PROFILE_TYPE);
        }

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(createEditUserDTO.getUsername());
        loginDTO.setPassword(createEditUserDTO.getPassword());
        try {
            emailService.sendMessage(createEditUserDTO.getEmail(),
                    "Account confirmation - Virtual Connect",
                    "Confirmation link - http://localhost:8081/account-confirmation/" + login("account-token", loginDTO));
        } catch (InvalidEmailException e){
            userRepository.delete(userReturned);
            throw new InvalidEmailException(e.getMessage());
        }

    }

    @Override
    public User update(String token, CreateEditUserDTO userDTO) {
        Optional<User> userOpt  = userRepository.findById(userDTO.getId());
        if (userOpt.isEmpty()){
            throw new EntityNotFoundException(format("User with id '%s' not found.",userDTO.getId()));
        }
        User user = userOpt.get();
        // if user didn't enter the password for approval of changes
        // or the password is wrong then return unauthorized
        if (userDTO.getPassword() == null || userDTO.getPassword().equals("") || !BCrypt.checkpw(userDTO.getPassword(), user.getPassword())){
            throw new UnauthorizedException("The current password is incorrect.");
        }
        // check if username is taken
        if (!user.getUsername().equals(userDTO.getUsername()) && userRepository.findByUsername(userDTO.getUsername()).isPresent()){
            throw new UsernameExistsException(format("Username '%s' is already in use.",userDTO.getUsername()));
        }
        // check if email is taken (shouldn't happen as user will have to confirm his email)
        if (!user.getEmail().equals(userDTO.getEmail()) && userRepository.findByEmail(userDTO.getEmail()).isPresent()){
            throw new EmailExistsException(format("Email '%s' is already in use.",userDTO.getEmail()));
        }

        user.setUsername(userDTO.getUsername());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setDateOfBirth(userDTO.getDateOfBirth());
        user.setCity(userDTO.getCity());
        user.setProfileDescription(userDTO.getProfileDescription());
        // verifying email with verification code that has been generated
        String secret = user.getPassword().substring(7,20) + userDTO.getEmail();
        String emailVerificationCode = userDTO.getEmailVerificationCode() != null ? userDTO.getEmailVerificationCode() :"";
        if (!user.getEmail().equals(userDTO.getEmail()) &&
                !bCryptPasswordEncoder.matches(secret, emailVerificationCode)){
            throw new UnauthorizedException("Email verification code is incorrect.");
        }
        user.setEmail(userDTO.getEmail());

        // if user also wants to change a password
        if (userDTO.getNewPassword() != null && !userDTO.getNewPassword().equals("")) {
            String newPassword = new BCryptPasswordEncoder().encode(userDTO.getNewPassword());
            user.setPassword(newPassword);
        }

        if (userDTO.getProfilePicture() != null && userDTO.getProfilePicture().equals("remove")){
            user.setProfilePicture(null);
        }
        else if(userDTO.getProfilePicture() != null && !userDTO.getProfilePicture().equals("") && userDTO.getPictureBase64() != null) {
            user.setProfilePicture(userDTO.getProfilePicture());
            this.uploadPicture(jwtTokenUtil.getUserId(token), userDTO.getProfilePicture(), userDTO.getPictureBase64(),this.PROFILE_TYPE);
        }

        return userRepository.save(user);
    }

    public void uploadPicture(Long userId, String pictureName, String pictureBase64, String type){
        var parts = pictureBase64.split(",");
        var imageString = parts[1];
        byte[] imageByte;
        imageByte = Base64.getDecoder().decode(imageString);

        String path = this.PHOTOS_PATH + userId+ "/" +type;
        File directory = new File(path);
        if (! directory.exists()){
            directory.mkdirs();
        }
        File outputfile = new File(path+"/"+pictureName);

        OutputStream os = null;
        try {
            os = new FileOutputStream(outputfile);
            os.write(imageByte);
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getProfilePicture(Long userId){
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()){
            throw new EntityNotFoundException("You are requesting picture for non-existing user.");
        }
        User user = userOpt.get();

        File file = new File("src/main/resources/user-photos/"+userId+"/"+this.PROFILE_TYPE+"/"+user.getProfilePicture());
        InputStream fileIS = null;
        try {
            fileIS = new FileInputStream(file);
            return StreamUtils.copyToByteArray(fileIS);

        } catch (FileNotFoundException e) {
            throw new EntityNotFoundException("The requested file does not exist.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public byte[] getPostPicture(String token, Long postId){
        Long sessionUserId = jwtTokenUtil.getUserId(token);
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()){
            throw new EntityNotFoundException("You are requesting picture for non-existing post.");
        }
        Post post = postOpt.get();
        Long postOwnerId = post.getUser().getId();
        String pictureName = post.getPicture();

        if (!sessionUserId.equals(postOwnerId) && post.getVisibility().equals("FRIENDS")){
            if (!areFriends(sessionUserId, postOwnerId)){
                throw new UnauthorizedException("You are not authorized for this action.");
            }
        }
        else if (!sessionUserId.equals(postOwnerId) && post.getVisibility().equals("ME")){
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        File file = new File("src/main/resources/user-photos/"+postOwnerId+"/"+this.POST_TYPE+"/"+pictureName);
        InputStream fileIS = null;
        try {
            fileIS = new FileInputStream(file);
            return StreamUtils.copyToByteArray(fileIS);

        } catch (FileNotFoundException e) {
            throw new EntityNotFoundException("The requested file does not exist.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
