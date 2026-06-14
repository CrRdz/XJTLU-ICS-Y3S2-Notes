package com.cpt202.service;

import com.cpt202.model.entity.User;
import com.cpt202.security.AuthContext;

/**
 * Handles JWT creation and parsing for authenticated users.
 */
public interface JwtTokenService {

    String generateToken(User user);

    AuthContext parseToken(String token);
}
