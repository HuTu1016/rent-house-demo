package com.renthouse.common.config;

import com.renthouse.auth.AppUser;
import com.renthouse.auth.AppUserRepository;
import com.renthouse.auth.UserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("local")
public class DemoDataInitializer implements ApplicationRunner {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    public DemoDataInitializer(AppUserRepository users, PasswordEncoder passwordEncoder) { this.users = users; this.passwordEncoder = passwordEncoder; }
    @Override @Transactional
    public void run(ApplicationArguments args) {
        createIfMissing(1001L, UserRole.LANDLORD, "13800000001", "演示房东");
        createIfMissing(1002L, UserRole.TENANT, "13800000002", "演示租客");
    }
    private void createIfMissing(long id, UserRole role, String mobile, String nickname) {
        if (users.findById(id).isEmpty()) users.save(new AppUser(id, role, mobile, passwordEncoder.encode("demo123"), nickname));
    }
}
