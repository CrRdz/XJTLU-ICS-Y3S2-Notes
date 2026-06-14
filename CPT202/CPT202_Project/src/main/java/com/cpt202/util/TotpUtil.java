package com.cpt202.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * Minimal TOTP helper based on RFC 6238 (SHA-1, 30-second step, 6 digits).
 */
public final class TotpUtil {

    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SECRET_SIZE_BYTES = 20;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;

    private TotpUtil() {
    }

    public static String generateBase32Secret() {
        byte[] buffer = new byte[SECRET_SIZE_BYTES];
        SECURE_RANDOM.nextBytes(buffer);
        return base32Encode(buffer);
    }

    public static boolean isValidCode(String base32Secret, String code) {
        if (base32Secret == null || code == null || !code.matches("\\d{6}")) {
            return false;
        }
        long currentWindow = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;
        for (long offset = -1; offset <= 1; offset++) {
            if (generateCode(base32Secret, currentWindow + offset).equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static String buildOtpAuthUri(String issuer, String accountName, String base32Secret) {
        String encodedIssuer = urlEncode(issuer);
        String encodedAccount = urlEncode(accountName);
        return "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30"
                .formatted(encodedIssuer, encodedAccount, base32Secret, encodedIssuer);
    }

    private static String generateCode(String base32Secret, long counter) {
        try {
            byte[] key = base32Decode(base32Secret);
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%06d", otp);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate TOTP code", ex);
        }
    }

    private static String base32Encode(byte[] data) {
        StringBuilder builder = new StringBuilder((data.length * 8 + 4) / 5);
        int current = 0;
        int bits = 0;
        for (byte value : data) {
            current = (current << 8) | (value & 0xFF);
            bits += 8;
            while (bits >= 5) {
                builder.append(BASE32_ALPHABET.charAt((current >> (bits - 5)) & 0x1F));
                bits -= 5;
            }
        }
        if (bits > 0) {
            builder.append(BASE32_ALPHABET.charAt((current << (5 - bits)) & 0x1F));
        }
        return builder.toString();
    }

    private static byte[] base32Decode(String value) {
        String normalized = value.replace("=", "").replace(" ", "").toUpperCase();
        byte[] output = new byte[normalized.length() * 5 / 8];
        int current = 0;
        int bits = 0;
        int index = 0;
        for (char ch : normalized.toCharArray()) {
            int digit = BASE32_ALPHABET.indexOf(ch);
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid base32 character");
            }
            current = (current << 5) | digit;
            bits += 5;
            if (bits >= 8) {
                output[index++] = (byte) ((current >> (bits - 8)) & 0xFF);
                bits -= 8;
            }
        }
        return output;
    }

    private static String urlEncode(String value) {
        StringBuilder builder = new StringBuilder();
        for (byte ch : value.getBytes(StandardCharsets.UTF_8)) {
            if ((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9')
                    || ch == '-' || ch == '_' || ch == '.' || ch == '~') {
                builder.append((char) ch);
            } else {
                builder.append('%').append(String.format("%02X", ch & 0xFF));
            }
        }
        return builder.toString();
    }
}
