package com.example.shopapp.services;

import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.exceptions.ExpiredTokenException;
import com.example.shopapp.models.Token;
import com.example.shopapp.models.User;
import com.example.shopapp.repositories.TokenRepository;
import com.example.shopapp.services.interfaces.ITokenService;
import com.example.shopapp.shared.utils.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {
    private static final int MAX_TOKENS = 3;
    @Value("${jwt.expiration}")
    private int expiration; //save to an environment variable

    @Value("${jwt.expiration-refresh-token}")
    private int expirationRefreshToken;

    private final TokenRepository tokenRepository;
    private final JwtUtils jwtUtils;
    @Transactional
    @Override
    //add vao table Token tu token login
    public Token addToToken(User user, String token, boolean isMobileDevice) {
        List<Token> userTokens = tokenRepository.findByUser(user);
        int tokenCount = userTokens.size();
        // Số lượng token vượt quá giới hạn, xóa một token cũ
        if (tokenCount >= MAX_TOKENS) {
            //kiểm tra xem trong danh sách userTokens có tồn tại ít nhất
            //một token không phải là thiết bị di động (none-mobile)
            boolean existNoneMobileToken = !userTokens.stream().allMatch(Token::isMobile);
            Token tokenToDelete;
            if (existNoneMobileToken) { //if more than 1 token are not of login by mobile
                tokenToDelete = userTokens.stream()
                        .filter(userToken -> !userToken.isMobile()) //ưu tiên xóa token ko là mobile
                        .findFirst() //tìm token cũ nhất sau khi lọc
                        .orElse(userTokens.get(0));
            } else {
                //tất cả các token đều là thiết bị di động,
                //xóa token đầu tiên trong danh sách
                tokenToDelete = userTokens.get(0);
            }
            tokenRepository.delete(tokenToDelete);
        }
        //add thêm hạn token tính từ lúc login
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);
        // Tạo mới một token cho người dùng
        Token newToken = Token.builder()
                .user(user)
                .token(token)
                .revoked(false)
                .expired(false)
                .tokenType("Bearer")
                .expirationDate(expirationDateTime)
                .isMobile(isMobileDevice)
                .build();

        newToken.setRefreshToken(UUID.randomUUID().toString());
        newToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));
        tokenRepository.save(newToken);
        return newToken;
    }

    @Transactional
    @Override
    public Token refreshToken(String refreshToken, User user) throws Exception {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
        if (existingToken == null) {
            throw new DataNotFoundException("Refresh token does not exist");
        }
        if (existingToken.getRefreshExpirationDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(existingToken); //het han -> xoa token
            throw new ExpiredTokenException("Refresh token is expired");
        }
        //neu refresh token con` han -> refresher tiep
        String token = jwtUtils.generateToken(user.getPhoneNumber()); //tao token moi
        // add them tgian cho token (nhung dua tren tgian hien tai chu ko add tgian lien tuc)
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);
        existingToken.setToken(token);
        existingToken.setExpirationDate(expirationDateTime);
        existingToken.setRefreshToken(refreshToken);
        existingToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));

        return existingToken;
    }
}
