package com.example.shopapp.services.interfaces;

import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;

public interface ITokenService {
    Token addToToken(User user, String token, boolean isMobileDevice);
    Token refreshToken(String refreshToken, User user) throws Exception;
}
