package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.constant.JwtClaimsConstant;
import com.cpt202.constant.SecurityConstants;
import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.model.entity.User;
import com.cpt202.properties.JwtProperties;
import com.cpt202.security.AuthContext;
import com.cpt202.security.UserAuthState;
import com.cpt202.service.JwtTokenService;
import com.cpt202.service.UserAuthStateService;
import com.cpt202.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

/**
 * Default JWT token service.
 */
@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final UserAuthStateService userAuthStateService;
    private final JwtProperties jwtProperties;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        signingKey = JwtUtil.createSigningKey(jwtProperties.getSecret());
    }

    @Override
    public String generateToken(User user) {
        return JwtUtil.createToken(signingKey, jwtProperties.getExpirationSeconds(), user);
    }

    @Override
    public AuthContext parseToken(String token) {
        Claims claims;
        try {
            claims = JwtUtil.parseToken(signingKey, token);
        } catch (Exception ex) {
            throw new UnauthorizedAccessException(MessageConstants.INVALID_LOGIN_STATE);
        }

        Long userId = claims.get(JwtClaimsConstant.USER_ID, Long.class);
        String roleValue = claims.get(JwtClaimsConstant.ROLE, String.class);
        User.UserRole tokenRole = parseRole(roleValue);

        UserAuthState user = userAuthStateService.getUserAuthState(userId);

        if (user.role() != tokenRole) {
            throw new UnauthorizedAccessException(MessageConstants.ROLE_MISMATCH);
        }
        if (!SecurityConstants.ACTIVE_ACCOUNT_STATUS.equalsIgnoreCase(user.accountStatus())) {
            throw new UnauthorizedAccessException(MessageConstants.ACCOUNT_DISABLED);
        }

        return new AuthContext(user.userId(), user.role());
    }

    private User.UserRole parseRole(String roleValue) {
        try {
            return User.UserRole.valueOf(roleValue);
        } catch (Exception ex) {
            throw new UnauthorizedAccessException(MessageConstants.INVALID_LOGIN_STATE);
        }
    }
}
