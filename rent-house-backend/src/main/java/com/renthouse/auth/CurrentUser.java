package com.renthouse.auth;

import com.renthouse.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() { }
    public static AuthenticatedUser require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new BusinessException("UNAUTHORIZED", "请先登录", HttpStatus.UNAUTHORIZED);
        }
        return user;
    }
    public static void requireRole(UserRole role) {
        if (require().role() != role) throw new BusinessException("FORBIDDEN", "无权执行此操作", HttpStatus.FORBIDDEN);
    }
}
