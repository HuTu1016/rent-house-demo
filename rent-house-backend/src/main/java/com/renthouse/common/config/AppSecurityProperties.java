package com.renthouse.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(String jwtSecret, long accessTokenMinutes, long refreshTokenDays) {
}
