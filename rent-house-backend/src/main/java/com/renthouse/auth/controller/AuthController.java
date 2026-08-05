package com.renthouse.auth.controller;

import com.renthouse.auth.service.AuthService;
import com.renthouse.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 账号认证与 Token 控制器
 * <p>
 * 提供手机号密码登录、Token 刷新与凭证获取服务。
 */
@Tag(name = "00. 认证与登录", description = "用户登录认证、Token 刷新及安全凭证接口")
@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 手机号密码登录
     *
     * @param request 密码登录请求体
     * @return Token 及用户信息
     */
    @Operation(summary = "手机号密码登录", description = "通过注册手机号和密码校验登录，返回 AccessToken 及 RefreshToken")
    @PostMapping("/password/login")
    public ApiResponse<AuthService.TokenView> passwordLogin(@RequestBody @Validated PasswordLoginRequest request) {
        return ApiResponse.ok(authService.passwordLogin(request.mobile(), request.password()));
    }

    /**
     * 刷新 Access Token
     *
     * @param request 刷新 Token 请求体
     * @return 新的 Token 及用户信息
     */
    @Operation(summary = "刷新登录令牌", description = "在 AccessToken 过期时使用有效 RefreshToken 换取新的 AccessToken")
    @PostMapping("/refresh")
    public ApiResponse<AuthService.TokenView> refresh(@RequestBody @Validated RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }

    /**
     * 密码登录请求参数对象
     */
    @Schema(description = "手机号密码登录请求参数")
    public record PasswordLoginRequest(
            @Schema(description = "11位手机号码", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            @Pattern(regexp = "^1\\d{10}$", message = "请输入11位手机号")
            String mobile,

            @Schema(description = "登录密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            String password
    ) { }

    /**
     * 刷新 Token 请求参数对象
     */
    @Schema(description = "刷新 Token 请求参数")
    public record RefreshRequest(
            @Schema(description = "刷新令牌 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            String refreshToken
    ) { }
}
