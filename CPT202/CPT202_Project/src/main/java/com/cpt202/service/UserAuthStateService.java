package com.cpt202.service;

import com.cpt202.security.UserAuthState;

public interface UserAuthStateService {

    UserAuthState getUserAuthState(Long userId);

    void evictUserAuthState(Long userId);
}
