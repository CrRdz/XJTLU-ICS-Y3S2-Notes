package com.cpt202.service;

import com.cpt202.model.entity.User;

public interface EmailOtpMailService {

    void sendLoginOtpMail(User user, String otp);

    void sendRegisterOtpMail(String email, String otp);
}
