package com.renthouse.tenant.service;

import com.renthouse.tenant.TenantController.Home;
import com.renthouse.tenant.TenantController.Profile;
import com.renthouse.tenant.TenantController.IdentityRequest;

public interface TenantService {
    Home getHomeData();
    Profile getProfileData(long tenantId);
    void updateIdentity(long tenantId, IdentityRequest request);
    Profile getTenantIdentityForAgent(long agentId, long tenantId);
}
