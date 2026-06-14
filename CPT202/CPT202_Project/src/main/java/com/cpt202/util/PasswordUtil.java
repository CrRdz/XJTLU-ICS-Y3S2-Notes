package com.cpt202.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 密码哈希工具类。
 * <p>
 * 统一管理密码的哈希与比对逻辑，当前使用 SHA-256，后续可无缝迁移至 BCrypt。
 */
@Component
public class PasswordUtil {

    /**
     * 对明文密码进行 SHA-256 哈希。
     *
     * @param rawPassword 明文密码
     * @return 十六进制哈希字符串
     */
    public String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to hash password", ex);
        }
    }

    /**
     * 校验明文密码是否与已存储的哈希值匹配。
     *
     * @param rawPassword 明文密码
     * @param hashed      已存储的哈希值
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String hashed) {
        return hash(rawPassword).equals(hashed);
    }
}
