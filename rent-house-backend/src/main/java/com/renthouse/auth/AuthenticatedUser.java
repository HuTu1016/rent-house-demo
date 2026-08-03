package com.renthouse.auth;

public record AuthenticatedUser(Long id, UserRole role, String nickname) {
}
