package com.cpt202.constant;

public final class RedisKeyConstants {

    public static final String CATEGORY_LIST = "cpt202:category:list";
    public static final String TAG_LIST = "cpt202:tag:list";
    public static final String USER_AUTH_STATE_PREFIX = "cpt202:user:auth:";
    public static final String EMAIL_LOGIN_OTP_PREFIX = "cpt202:auth:email-otp:";
    public static final String EMAIL_REGISTER_OTP_PREFIX = "cpt202:auth:email-register-otp:";
    public static final String EMAIL_LOGIN_OTP_COOLDOWN_PREFIX = "cpt202:auth:email-otp:cooldown:";
    public static final String EMAIL_REGISTER_OTP_COOLDOWN_PREFIX = "cpt202:auth:email-register-otp:cooldown:";
    public static final String TWO_FACTOR_SETUP_PREFIX = "cpt202:auth:2fa:setup:";
    public static final String TWO_FACTOR_LOGIN_CHALLENGE_PREFIX = "cpt202:auth:2fa:challenge:";

    private RedisKeyConstants() {
    }
}
