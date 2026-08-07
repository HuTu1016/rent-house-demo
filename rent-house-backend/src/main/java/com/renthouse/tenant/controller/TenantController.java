package com.renthouse.tenant.controller;

import com.renthouse.auth.enums.UserRole;
import com.renthouse.auth.service.CurrentUser;
import com.renthouse.common.api.ApiResponse;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.tenant.service.TenantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class TenantController {
    private final TenantService service;

    public TenantController(TenantService service) { this.service = service; }

    @GetMapping("/tenant/home")
    public ApiResponse<Home> home() { return ApiResponse.ok(service.getHomeData()); }

    @GetMapping("/tenant/profile")
    public ApiResponse<Profile> profile() {
        CurrentUser.requireRole(UserRole.TENANT);
        Profile profile = service.getProfileData(CurrentUser.require().id());
        if (profile == null) throw new BusinessException("USER_NOT_FOUND", "用户不存在", HttpStatus.NOT_FOUND);
        return ApiResponse.ok(profile);
    }

    @PatchMapping("/tenant/profile")
    public ApiResponse<Void> update(@RequestBody @Valid IdentityRequest request) {
        CurrentUser.requireRole(UserRole.TENANT);
        service.updateIdentity(CurrentUser.require().id(), request);
        return ApiResponse.ok();
    }

    @GetMapping("/agent/tenants/{tenantId}/profile")
    public ApiResponse<Profile> agentProfile(@PathVariable long tenantId) {
        CurrentUser.requireRole(UserRole.AGENT);
        Profile profile = service.getTenantIdentityForAgent(CurrentUser.require().id(), tenantId);
        if (profile == null) throw new BusinessException("TENANT_NOT_FOUND", "租客资料不存在或无权查看", HttpStatus.NOT_FOUND);
        return ApiResponse.ok(profile);
    }

    public record Special(String id, String title, int rentCent) { }
    public record Home(java.util.List<Special> specials, java.util.Map<String, Long> stats) { }
    public record Profile(String nickname, String avatarUrl, String mobile, String realName, long favorites, long histories) { }
    public record IdentityRequest(@NotBlank(message = "姓名不能为空") String realName,
                                  @NotBlank(message = "手机号不能为空") @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确") String mobile) { }
}
