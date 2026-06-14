package com.cpt202.service;

import com.cpt202.model.entity.User;

public interface PasswordResetMailService {

    void sendPasswordResetMail(User user, String resetLink);
}
