package com.ftn.socialnetwork.service.implementation;

import com.ftn.socialnetwork.model.User;
import com.ftn.socialnetwork.model.dto.RegistrationDTO;
import com.ftn.socialnetwork.model.dto.UserDTO;
import com.ftn.socialnetwork.repository.FriendRequestRepository;
import com.ftn.socialnetwork.repository.UserRepository;
import com.ftn.socialnetwork.security.jwt.JwtTokenUtil;
import com.ftn.socialnetwork.service.IUserService;
import com.ftn.socialnetwork.util.exception.EmailExistsException;
import com.ftn.socialnetwork.util.exception.EntityNotFoundException;
import com.ftn.socialnetwork.util.exception.UnauthorizedException;
import com.ftn.socialnetwork.util.exception.UsernameExistsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.lang.String.format;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final FriendRequestRepository friendRequestRepository;

    public UserService(UserRepository userRepository, JwtTokenUtil jwtTokenUtil, FriendRequestRepository friendRequestRepository) {
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.friendRequestRepository = friendRequestRepository;
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
    public List<User> findUsers(String searchTerm) {
        return userRepository.searchByFirstNameLastNameUsername(searchTerm);
    }


    public Boolean areFriends(Long user1id, Long user2id){
        return friendRequestRepository.findBySenderIdAndReceiverIdAndRequestStatus(user1id, user2id, "ACCEPTED").isPresent() ||
                friendRequestRepository.findBySenderIdAndReceiverIdAndRequestStatus(user2id, user1id, "ACCEPTED").isPresent();
    }


    @Override
    public User getUserData(String token, Long id) {
        // if user is not checking his own data, validate if he's a friend of a user
        // whose data he's trying to access
        if (!Long.valueOf(jwtTokenUtil.getUserId(token)).equals(id) &&
                !this.areFriends(Long.valueOf(jwtTokenUtil.getUserId(token)), id)) {
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        Optional<User> user  = userRepository.findById(id);
        if (user.isEmpty()){
            throw new EntityNotFoundException(format("User with id '%s' not found.",id));
        }

        return user.get();
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User save(RegistrationDTO registrationDTO) {

        // check if username is taken
        if (userRepository.findByUsername(registrationDTO.getUsername()).isPresent()){
            throw new UsernameExistsException(format("Username '%s' is already in use.",registrationDTO.getUsername()));
        }
        // check if email is taken (shouldn't happen as user will have to confirm his email)
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()){
            throw new EmailExistsException(format("Email '%s' is already in use.",registrationDTO.getUsername()));
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        String password = new BCryptPasswordEncoder().encode(registrationDTO.getPassword());
        user.setPassword(password);
        user.setEmail(registrationDTO.getEmail());
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setDateOfBirth(registrationDTO.getDateOfBirth());
        user.setCity(registrationDTO.getCity());


        return userRepository.save(user);
    }

    @Override
    public User update(String token, UserDTO userDTO) {

        if (!Long.valueOf(jwtTokenUtil.getUserId(token)).equals(userDTO.getId())) {
            throw new UnauthorizedException("You are not authorized for this action.");
        }

        Optional<User> userOpt  = userRepository.findById(userDTO.getId());
        if (userOpt.isEmpty()){
            throw new EntityNotFoundException(format("User with username '%s' not found.",userDTO.getUsername()));
        }
        User user = userOpt.get();

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
        user.setEmail(userDTO.getEmail());
        user.setDateOfBirth(userDTO.getDateOfBirth());
        user.setCity(userDTO.getCity());
        user.setProfilePicture(userDTO.getProfilePicture());

        if (userDTO.getPassword() != null && !userDTO.getPassword().equals("")) {
            String newPassword = new BCryptPasswordEncoder().encode(userDTO.getPassword());
            System.out.println("New raw password: "+ userDTO.getPassword());
            System.out.println("New encoded password: "+ newPassword);
            user.setPassword(newPassword);
        }

        return userRepository.save(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
