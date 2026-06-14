package com.cpt202.security;

import com.cpt202.model.entity.User;

public record AuthContext(Long userId, User.UserRole role) {
}
