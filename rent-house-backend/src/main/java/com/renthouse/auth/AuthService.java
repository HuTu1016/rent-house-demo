package com.renthouse.auth;

import com.renthouse.common.exception.BusinessException;
import com.renthouse.common.id.SnowflakeIdGenerator;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SnowflakeIdGenerator idGenerator;
    public AuthService(AppUserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, SnowflakeIdGenerator idGenerator) {
        this.userRepository = userRepository; this.passwordEncoder = passwordEncoder; this.jwtService = jwtService; this.idGenerator = idGenerator;
    }
    @Transactional
    public TokenView passwordLogin(String mobile, String password) {
        AppUser user = userRepository.findByMobileAndDeletedAtIsNull(mobile)
                .orElseThrow(() -> new BusinessException("LOGIN_FAILED", "手机号或密码错误", HttpStatus.UNAUTHORIZED));
        if (user.getStatus() != UserStatus.ACTIVE) throw new BusinessException("ACCOUNT_DISABLED", "账号不可用", HttpStatus.FORBIDDEN);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) throw new BusinessException("LOGIN_FAILED", "手机号或密码错误", HttpStatus.UNAUTHORIZED);
        user.markLoggedIn();
        return issue(user);
    }
    @Transactional
    public TokenView demoLogin(UserRole role) {
        UserRole selectedRole = role == null ? UserRole.TENANT : role;
        String mobile = selectedRole == UserRole.LANDLORD ? "13800000001" : "13800000002";
        AppUser user = userRepository.findByMobileAndDeletedAtIsNull(mobile).orElseGet(() -> {
            AppUser created = new AppUser(idGenerator.nextId(), selectedRole, mobile, passwordEncoder.encode("demo123"), selectedRole == UserRole.LANDLORD ? "演示房东" : "演示租客");
            return userRepository.save(created);
        });
        user.markLoggedIn();
        return issue(user);
    }
    @Transactional(readOnly = true)
    public TokenView refresh(String refreshToken) {
        AuthenticatedUser principal;
        try { principal = jwtService.parseRefreshToken(refreshToken); }
        catch (RuntimeException exception) { throw new BusinessException("REFRESH_TOKEN_INVALID", "登录已过期，请重新登录", HttpStatus.UNAUTHORIZED); }
        AppUser user = userRepository.findById(principal.id()).orElseThrow(() -> new BusinessException("REFRESH_TOKEN_INVALID", "账号不存在", HttpStatus.UNAUTHORIZED));
        if (user.getStatus() != UserStatus.ACTIVE) throw new BusinessException("ACCOUNT_DISABLED", "账号不可用", HttpStatus.FORBIDDEN);
        return issue(user);
    }
    private TokenView issue(AppUser user) {
        return new TokenView(jwtService.createAccessToken(user), jwtService.createRefreshToken(user), "Bearer", user.getId().toString(), user.getRole().name(), user.getNickname());
    }
    public record TokenView(String accessToken, String refreshToken, String tokenType, String userId, String role, String nickname) { }
}
