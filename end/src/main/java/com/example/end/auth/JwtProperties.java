package com.example.end.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret = "change-this-secret-key";

    private long expireSeconds = 86400;
}
