package com.renthouse.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user")
public class AppUser {
    @Id private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private UserRole role;
    private String mobile;
    @Column(name = "password_hash") private String passwordHash;
    @Column(name = "wechat_openid") private String wechatOpenid;
    @Column(nullable = false) private String nickname;
    @Column(name = "avatar_url") private String avatarUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private UserStatus status;
    @Column(name = "last_login_at") private LocalDateTime lastLoginAt;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;

    protected AppUser() { }
    public AppUser(Long id, UserRole role, String mobile, String passwordHash, String nickname) {
        this.id = id; this.role = role; this.mobile = mobile; this.passwordHash = passwordHash; this.nickname = nickname;
        this.status = UserStatus.ACTIVE; this.createdAt = LocalDateTime.now(); this.updatedAt = this.createdAt;
    }
    public Long getId() { return id; }
    public UserRole getRole() { return role; }
    public String getMobile() { return mobile; }
    public String getPasswordHash() { return passwordHash; }
    public String getNickname() { return nickname; }
    public UserStatus getStatus() { return status; }
    public void markLoggedIn() { this.lastLoginAt = LocalDateTime.now(); this.updatedAt = this.lastLoginAt; }
}
