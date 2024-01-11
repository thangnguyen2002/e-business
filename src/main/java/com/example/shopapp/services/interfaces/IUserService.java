package com.example.shopapp.services.interfaces;

import com.example.shopapp.dtos.UpdateUserDTO;
import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.InvalidPasswordException;
import com.example.shopapp.models.User;
import com.example.shopapp.responses.UserResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(String phoneNumber, String password) throws Exception;
    User getUserDetailsFromToken(String token) throws Exception;

    User getUserDetailsFromRefreshToken(String refreshToken) throws Exception;

    Page<UserResponse> findAll(String keyword, PageRequest pageRequest);
    User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception;

    void resetPassword(Long userId, String newPassword) throws InvalidPasswordException, DataNotFoundException;

    @Transactional
    void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException;
}
