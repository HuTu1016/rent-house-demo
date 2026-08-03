package com.renthouse.auth;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.time.LocalDateTime;

@TableName("sys_user")
public class AppUser {
    @TableId private Long id;
    private UserRole role;
    private String mobile;
    private String passwordHash;
    private String wechatOpenid;
    private String nickname;
    private String avatarUrl;
    private UserStatus status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic private LocalDateTime deletedAt;

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
