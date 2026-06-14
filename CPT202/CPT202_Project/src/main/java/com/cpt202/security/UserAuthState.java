package com.cpt202.security;

import com.cpt202.model.entity.User;

/**
 * Lightweight auth snapshot cached in Redis.
 */
public record UserAuthState(Long userId, User.UserRole role, String accountStatus) {
}
