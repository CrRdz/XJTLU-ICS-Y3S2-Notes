package com.cpt202.service;

import com.cpt202.model.entity.User;
import com.cpt202.vo.TwoFactorSetupVO;

public interface TwoFactorAuthService {

    TwoFactorSetupVO initializeSetup(User user);

    void enable(User user, String code);

    void disable(User user);

    String createLoginChallenge(User user);

    User verifyLoginChallenge(String challengeToken, String code);
}
