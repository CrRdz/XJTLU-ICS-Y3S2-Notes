package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.constant.RedisKeyConstants;
import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.repository.UserRepository;
import com.cpt202.security.UserAuthState;
import com.cpt202.service.RedisCacheService;
import com.cpt202.service.UserAuthStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserAuthStateServiceImpl implements UserAuthStateService {

    private static final Duration USER_AUTH_TTL = Duration.ofMinutes(10);

    private final UserRepository userRepository;
    private final RedisCacheService redisCacheService;

    @Override
    public UserAuthState getUserAuthState(Long userId) {
        String key = RedisKeyConstants.USER_AUTH_STATE_PREFIX + userId;
        return redisCacheService.get(key, UserAuthState.class)
                .orElseGet(() -> {
                    UserAuthState userAuthState = userRepository.findAuthStateByUserId(userId)
                            .orElseThrow(() -> new UnauthorizedAccessException(MessageConstants.USER_NOT_FOUND_OR_DENIED));
                    redisCacheService.set(key, userAuthState, USER_AUTH_TTL);
                    return userAuthState;
                });
    }

    @Override
    public void evictUserAuthState(Long userId) {
        redisCacheService.delete(RedisKeyConstants.USER_AUTH_STATE_PREFIX + userId);
    }
}
