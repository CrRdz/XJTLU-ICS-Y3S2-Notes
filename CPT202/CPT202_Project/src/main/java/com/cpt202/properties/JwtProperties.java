package com.cpt202.properties;

import com.cpt202.constant.SecurityConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration bound from application properties.
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private long expirationSeconds;
    private String tokenName = SecurityConstants.DEFAULT_TOKEN_NAME;
    private String tokenPrefix = SecurityConstants.DEFAULT_TOKEN_PREFIX;
}
