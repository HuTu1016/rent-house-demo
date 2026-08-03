package com.renthouse.tenant.service;

import com.renthouse.tenant.controller.TenantController.Home;
import com.renthouse.tenant.controller.TenantController.Profile;
import com.renthouse.tenant.controller.TenantController.IdentityRequest;

public interface TenantService {
    Home getHomeData();
    Profile getProfileData(long tenantId);
    void updateIdentity(long tenantId, IdentityRequest request);
    Profile getTenantIdentityForAgent(long agentId, long tenantId);
}
