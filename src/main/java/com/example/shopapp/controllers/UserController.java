package com.example.shopapp.controllers;

import com.example.shopapp.dtos.UserDTO;
import com.example.shopapp.dtos.UserLoginDTO;
import com.example.shopapp.services.interfaces.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private final IUserService iUserService;
    @PostMapping("/register")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result
    ) {
        try {
            if (result.hasErrors()) {
                List<String> errorMsg = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
            }
            //check retype password
            if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                return new ResponseEntity<>("Password does not match", HttpStatus.BAD_REQUEST);
            }
            iUserService.createUser(userDTO);
            return new ResponseEntity<>("Register successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login (@Valid @RequestBody UserLoginDTO userLoginDTO) {
        try{
            String token = iUserService.login(userLoginDTO.getPhoneNumber(),userLoginDTO.getPassword());
            return new ResponseEntity<String>(token, HttpStatus.OK);
        }
        catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}
