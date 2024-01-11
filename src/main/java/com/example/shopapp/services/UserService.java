package com.example.shopapp.services;

import com.example.shopapp.dtos.UpdateUserDTO;
import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.InvalidPasswordException;
import com.example.shopapp.models.Role;
import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.RoleRepository;
import com.example.shopapp.repositories.TokenRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.responses.UserResponse;
import com.example.shopapp.services.interfaces.IUserService;
import com.example.shopapp.shared.utils.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final RoleRepository roleRepository;

    @Autowired
    private final TokenRepository tokenRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final UserDetailsService userDetailsService;

    @Autowired
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNum = userDTO.getPhoneNumber();
        // Kiểm tra xem số điện thoại đã tồn tại hay chưa
        if (userRepository.existsByPhoneNumber(phoneNum)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        Role existingRole = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new DataIntegrityViolationException("Role not found"));
//        if(existingRole.getName().toUpperCase().equals(Role.ADMIN)) {
//            throw new PermissionDenyException("You cannot register an admin account");
//        }
        //convert from userDTO => user
        User newUser = User
                .builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .address(userDTO.getAddress())
                .password(userDTO.getPassword())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .active(true)
                .role(existingRole)
                .build();
        // Kiểm tra nếu có accountId, không yêu cầu password
        if (userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0) {
            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
            newUser.setPassword(encodedPassword);
        }
        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password) throws Exception {
        //Authenticate
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByPhoneNumber(phoneNumber));
        if (optionalUser.isEmpty()) {
            throw new DataNotFoundException("Invalid phone number or password");
        }
        User exUser = optionalUser.get();
        //check password
        if (exUser.getFacebookAccountId() == 0 && exUser.getGoogleAccountId() == 0) {
            if (!passwordEncoder.matches(password, exUser.getPassword())) {
                throw new BadCredentialsException("Wrong phone number or password");
            }
        }
        if (!exUser.isActive()) {
            throw new DataNotFoundException("Your account has been locked!!");
        }
        //Generate JWT TOKEN
        return jwtUtils.generateToken(exUser.getPhoneNumber());
    }

    @Override
    public User getUserDetailsFromToken(String token) throws Exception {
        if (jwtUtils.isTokenExpired(token)) {
            throw new Exception("Token is expired");
        }
        String phoneNumber = jwtUtils.extractPhoneNumber(token);
        Optional<User> exUser = Optional.ofNullable(userRepository.findByPhoneNumber(phoneNumber));
        if (exUser.isPresent()) {
            return exUser.get();
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public User getUserDetailsFromRefreshToken(String refreshToken) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        return getUserDetailsFromToken(existingToken.getToken());
    }

    @Override
    public Page<UserResponse> findAll(String keyword, PageRequest pageRequest) {
        Page<User> userPage = userRepository.findAll(keyword, pageRequest);
        return userPage.map(UserResponse::fromUser); //map of Page, not Java stream, so cannot use stream

    }

    @Override
    @Transactional
    public User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception {
        User exUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        String newPhoneNumber = updatedUserDTO.getPhoneNumber();
        if (userRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        // Update user information based on the DTO
        if (updatedUserDTO.getFullName() != null) {
            exUser.setFullName(updatedUserDTO.getFullName());
        }
        if (updatedUserDTO.getPhoneNumber() != null) {
            exUser.setPhoneNumber(updatedUserDTO.getPhoneNumber());
        }
        if (updatedUserDTO.getAddress() != null) {
            exUser.setAddress(updatedUserDTO.getAddress());
        }
        if (updatedUserDTO.getDateOfBirth() != null) {
            exUser.setDateOfBirth(updatedUserDTO.getDateOfBirth());
        }
        if (updatedUserDTO.getFacebookAccountId() > 0) {
            exUser.setFacebookAccountId(updatedUserDTO.getFacebookAccountId());
        }
        if (updatedUserDTO.getGoogleAccountId() > 0) {
            exUser.setGoogleAccountId(updatedUserDTO.getGoogleAccountId());
        }
        // Update the password if it is provided in the DTO
        if (updatedUserDTO.getPassword() != null) {
            if (updatedUserDTO.getPassword().equals(updatedUserDTO.getRetypePassword())) {
                throw new DataNotFoundException("Password and retype password are different!!");
            }
            String newPassword = updatedUserDTO.getPassword();
            String encodePassword = passwordEncoder.encode(newPassword);
            exUser.setPassword(encodePassword);
        }
        // Save the updated user
        return userRepository.save(exUser);
    }

    @Transactional
    @Override
    public void resetPassword(Long userId, String newPassword) throws DataNotFoundException, InvalidPasswordException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        String encodedPassword = passwordEncoder.encode(newPassword);
        existingUser.setPassword(encodedPassword);
        userRepository.save(existingUser);
        //reset password => clear old tokens -> user currently login is instantly logout
        List<Token> tokens = tokenRepository.findByUser(existingUser);
        for (Token token : tokens) {
            tokenRepository.delete(token);
        }
    }

    @Override
    @Transactional
    public void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        existingUser.setActive(active);
        userRepository.save(existingUser);
    }
}
