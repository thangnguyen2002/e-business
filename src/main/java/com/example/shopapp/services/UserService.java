package com.example.shopapp.services;

import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.PermissionDenyException;
import com.example.shopapp.models.Role;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.RoleRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.services.interfaces.IUserService;
import com.example.shopapp.shared.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final RoleRepository roleRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final UserDetailsService userDetailsService;

    @Autowired
    private final JwtUtils jwtUtils;
    @Override
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNum = userDTO.getPhoneNumber();
        // Kiểm tra xem số điện thoại đã tồn tại hay chưa
        if (userRepository.existsByPhoneNumber(phoneNum)) {
            throw new DataIntegrityViolationException("Phone number already exists");
        }
        Role existingRole = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(()->new DataIntegrityViolationException("Role not found"));
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
                .role(existingRole)
                .build();
        // Kiểm tra nếu có accountId, không yêu cầu password
        if (userDTO.getFacebookAccountId()==0 && userDTO.getGoogleAccountId()==0) {
            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
            newUser.setPassword(encodedPassword);
        }
        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password) throws Exception {
        //Authenticate
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByPhoneNumber(phoneNumber));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getFacebookAccountId() == 0 && user.getGoogleAccountId()==0) {
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    throw new BadCredentialsException("Wrong phone number or password");
                }
            }
            //Generate JWT TOKEN
            return jwtUtils.generateToken(user.getPhoneNumber());
        }
        throw new DataNotFoundException("Invalid phone number or password");
    }
}
