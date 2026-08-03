package com.renthouse.tenant.service.impl;

import com.renthouse.tenant.TenantController.Home;
import com.renthouse.tenant.TenantController.Profile;
import com.renthouse.tenant.TenantController.IdentityRequest;
import com.renthouse.tenant.mapper.TenantMapper;
import com.renthouse.tenant.service.TenantService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TenantServiceImpl implements TenantService {

    private final TenantMapper mapper;

    public TenantServiceImpl(TenantMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Home getHomeData() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("published", mapper.countPublishedListings());
        stats.put("vacant", mapper.countVacantUnits());
        return new Home(mapper.getSpecials(), stats);
    }

    @Override
    public Profile getProfileData(long tenantId) {
        Profile p = mapper.getUserProfile(tenantId);
        if (p != null) {
            long favorites = mapper.countFavorites(tenantId);
            long histories = mapper.countHistories(tenantId);
            return new Profile(p.nickname(), p.avatarUrl(), p.mobile(), p.realName(), p.idNumberMasked(), p.homeAddress(), p.companyName(), p.companyAddress(), favorites, histories);
        }
        return null;
    }

    @Override
    public void updateIdentity(long tenantId, IdentityRequest request) {
        mapper.updateIdentity(tenantId, request);
    }

    @Override
    public Profile getTenantIdentityForAgent(long agentId, long tenantId) {
        return mapper.getTenantIdentityForAgent(agentId, tenantId);
    }
}
