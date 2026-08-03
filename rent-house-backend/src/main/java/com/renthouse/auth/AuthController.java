package com.renthouse.auth;

import com.renthouse.common.api.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/password/login")
    public ApiResponse<AuthService.TokenView> passwordLogin(@RequestBody @Validated PasswordLoginRequest request) {
        return ApiResponse.ok(authService.passwordLogin(request.mobile(), request.password()));
    }
    @PostMapping("/demo-login")
    public ApiResponse<AuthService.TokenView> demoLogin(@RequestBody @Validated DemoLoginRequest request) {
        return ApiResponse.ok(authService.demoLogin(request.role()));
    }
    @PostMapping("/refresh")
    public ApiResponse<AuthService.TokenView> refresh(@RequestBody @Validated RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }
    public record PasswordLoginRequest(@NotBlank @Pattern(regexp = "^1\\d{10}$", message = "请输入11位手机号") String mobile, @NotBlank String password) { }
    public record DemoLoginRequest(UserRole role) { }
    public record RefreshRequest(@NotBlank String refreshToken) { }
}
