package com.renthouse.auth.entity;

import com.renthouse.auth.enums.UserRole;

public record AuthenticatedUser(Long id, UserRole role, String nickname) {
}
