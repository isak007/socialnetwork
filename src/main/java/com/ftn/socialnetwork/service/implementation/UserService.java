package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.Post;
import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.CreateEditUserDTO;
import com.ftn.socialnetwork.model.dto.LoginDTO;
import com.ftn.socialnetwork.repository.FriendRequestRepository;
import com.ftn.socialnetwork.repository.PostRepository;
import com.ftn.socialnetwork.repository.UserRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.IUserService;
import com.ftn.socialnetwork.util.exception.EmailExistsException;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import com.ftn.socialnetwork.util.exception.UsernameExistsException;
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

import javax.imageio.ImageIO;
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
        // if user is not checking his own data, validate if he's a friend of a user
        // whose data he's trying to access
//        if (!jwtTokenUtil.getUserId(token).equals(id) &&
//                !this.areFriends(jwtTokenUtil.getUserId(token), id)) {
//            throw new UnauthorizedException("You are not authorized for this action.");
//        }

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

        // if user is not checking his own data, validate if he's a friend of a user
        // whose data he's trying to access
//        Long sessionUserId = jwtTokenUtil.getUserId(token);
//        if (!sessionUserId.equals(user.get().getId()) &&
//                !this.areFriends(sessionUserId, user.get().getId())) {
//            throw new UnauthorizedException("You are not authorized for this action.");
//        }

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
//        String verificationCode = BCrypt.hashpw(secret, BCrypt.gensalt(6));

        emailService.sendMessage(email,
                "New email verification - Virtual Connect",
                "Verification code: "+ verificationCode);
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
        emailService.sendMessage(createEditUserDTO.getEmail(),
                "Account confirmation - Virtual Connect",
                "http://localhost:8081/account-confirmation/"+ login("account-token",loginDTO));

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
            System.out.println("New raw password: "+ userDTO.getNewPassword());
            System.out.println("New encoded password: "+ newPassword);
            user.setPassword(newPassword);
        }

        if (userDTO.getProfilePicture() != null && userDTO.getProfilePicture().equals("remove")){
            user.setProfilePicture(null);
        }
        else if(userDTO.getProfilePicture() != null && !userDTO.getProfilePicture().equals("") && userDTO.getPictureBase64() != null) {
            //userDTO.setProfilePicture(userDTO.getProfilePicture().replace(" ",""));
            user.setProfilePicture(userDTO.getProfilePicture());
            this.uploadPicture(jwtTokenUtil.getUserId(token), userDTO.getProfilePicture(), userDTO.getPictureBase64(),this.PROFILE_TYPE);
        }

        return userRepository.save(user);
    }

    public void uploadPicture(Long userId, String pictureName, String pictureBase64, String type){
        var parts = pictureBase64.split(",");
        var imageString = parts[1];

        // create a buffered image
        BufferedImage image = null;
        byte[] imageByte;

        imageByte = Base64.getDecoder().decode(imageString);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
        try {
            image = ImageIO.read(bis);
            bis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String path = "src/main/resources/user-photos/"+userId+"/"+type;
        File directory = new File(path);
        if (! directory.exists()){
            //directory.mkdir();
            // If it requires to make the entire directory path including parents,
            directory.mkdirs();
        }

        File outputfile = new File(path+"/"+pictureName);

        System.out.println(outputfile.getAbsolutePath());

        try {
            String[] pictureSplit = pictureName.split("\\.");
            System.out.println(pictureSplit[pictureSplit.length-1]);
            ImageIO.write(image, pictureSplit[pictureSplit.length-1], outputfile);
        } catch (Exception e) {
            System.out.println(e);
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
