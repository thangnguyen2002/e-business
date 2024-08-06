package com.example.shopapp.controllers;

import com.example.shopapp.dtos.RefreshTokenDTO;
import com.example.shopapp.dtos.UpdateUserDTO;
import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.dtos.UserLoginDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.InvalidPasswordException;
import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.responses.LoginResponse;
import com.example.shopapp.responses.RegisterResponse;
import com.example.shopapp.responses.UserListResponse;
import com.example.shopapp.responses.UserResponse;
import com.example.shopapp.services.interfaces.ITokenService;
import com.example.shopapp.services.interfaces.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private final IUserService iUserService;
    @Autowired
    private final ITokenService iTokenService;
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) {
        RegisterResponse registerResponse = new RegisterResponse();

        if (result.hasErrors()) { //check loi trong DTO
            List<String> errorMsg = result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            registerResponse.setMessage(errorMsg.toString());
            return new ResponseEntity<>(registerResponse, HttpStatus.BAD_REQUEST);
        }
        //check retype password
        if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
            registerResponse.setMessage("Password does not match");
            return new ResponseEntity<>(registerResponse, HttpStatus.BAD_REQUEST);
        }
        try {
            User user = iUserService.createUser(userDTO);
            registerResponse.setUser(user);
            registerResponse.setMessage("Register successfully");
            return new ResponseEntity<>(registerResponse, HttpStatus.OK);
        } catch (Exception e) {
            registerResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(registerResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO,
            HttpServletRequest request) {
        // Kiểm tra thông tin đăng nhập và sinh token
        try {
            String token = iUserService.login(
                    userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword());

            String userAgent = request.getHeader("User-Agent");
            //login tu thiet bi nao (sua trong postman la mobile)
            User userDetail = iUserService.getUserDetailsFromToken(token);
            Token jwtToken = iTokenService.addToToken(userDetail, token, isMobileDevice(userAgent));

            return new ResponseEntity<>(LoginResponse.builder()
                    .message("Login successfully")
                    .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .userId(userDetail.getId())
                    .phoneNumber(userDetail.getPhoneNumber())
                    .role(userDetail.getRole().getName())
                    .build(),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(LoginResponse.builder()
                    .message(e.getMessage())
                    .build(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenDTO refreshTokenDTO
    ) {
        try {
            User userDetail = iUserService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());
            Token jwtToken = iTokenService.refreshToken(refreshTokenDTO.getRefreshToken(), userDetail);
            return ResponseEntity.ok(LoginResponse.builder()
                    .message("Refresh token successfully")
                    .token(jwtToken.getToken())
                    .tokenType(jwtToken.getTokenType())
                    .refreshToken(jwtToken.getRefreshToken())
                    .phoneNumber(userDetail.getPhoneNumber())
                    .role(userDetail.getRole().getName())
                    .userId(userDetail.getId())
                    .build());
        } catch (Exception e) {
            return new ResponseEntity<>(LoginResponse.builder()
                    .message(e.getMessage())
                    .build(), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isMobileDevice(String userAgent) {
        // Kiểm tra User-Agent header để xác định thiết bị di động
        return userAgent.toLowerCase().contains("mobile");
    }

    @GetMapping
    public ResponseEntity<?> getAllUser(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        try {
            PageRequest pageRequest = PageRequest.of(
                    page, limit,
                    Sort.by("createdAt").descending()
            );
            // Tạo Pageable từ thông tin trang và giới hạn
            Page<UserResponse> userPage = iUserService.findAll(keyword,pageRequest);
            //admin chi co the lay cac user, ko dc xem cac admin khac
        // Lấy tổng số trang
            int totalPages = userPage.getTotalPages();
            List<UserResponse> userResponses = userPage.getContent();
            return new ResponseEntity<>(UserListResponse
                    .builder()
                    .users(userResponses)
                    .totalPages(totalPages)
                    .build(),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/details")
    public ResponseEntity<?> getUserDetails(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7);
            // Loại bỏ "Bearer " từ chuỗi token, lay tu vi tri 7 tro di
            User user = iUserService.getUserDetailsFromToken(extractedToken);
            return new ResponseEntity<>(UserResponse.fromUser(user), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/details/{userId}")
    public ResponseEntity<?> updateUserDetails( //lay thong tin phoneNumber tu token -> lay dc thong tin user
            @PathVariable Long userId,
            @RequestBody UpdateUserDTO updatedUserDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7);
            User user = iUserService.getUserDetailsFromToken(extractedToken);
            //user do' chi duoc upadate chinh minh`
            if (!Objects.equals(user.getId(), userId)) {
                return new ResponseEntity<>("You have no permission", HttpStatus.FORBIDDEN);
            }
            User updatedUser = iUserService.updateUser(userId, updatedUserDTO);
            return new ResponseEntity<>(UserResponse.fromUser(updatedUser), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/reset-password/{userId}")
    public ResponseEntity<?> resetPassword(@Valid @PathVariable Long userId){
        try {
            String newPassword = UUID.randomUUID().toString().substring(0, 5); // Tạo mật khẩu mới
            iUserService.resetPassword(userId, newPassword);
            return new ResponseEntity<>(String.format("New password: %s", newPassword), HttpStatus.OK);
        } catch (InvalidPasswordException e) {
            return new ResponseEntity<>("Invalid password", HttpStatus.BAD_REQUEST);
        } catch (DataNotFoundException e) {
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/block/{userId}/{active}") //block account -> set account active or not (true 1 or false 0)
    public ResponseEntity<String> blockOrEnable(
            @Valid @PathVariable Long userId,
            @Valid @PathVariable Integer active //1 or 0
    ) {
        try {
           iUserService.blockOrEnable(userId, active > 0);
           String message = active > 0 ? "Successfully enabled the user." : "Successfully blocked the user.";
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (DataNotFoundException e) {
            return new ResponseEntity<>("User not found.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
