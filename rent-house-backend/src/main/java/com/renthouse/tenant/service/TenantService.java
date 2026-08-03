package com.renthouse.tenant.service;

import com.renthouse.tenant.TenantController.Home;
import com.renthouse.tenant.TenantController.Profile;

public interface TenantService {
    Home getHomeData();
    Profile getProfileData(long tenantId);
}
