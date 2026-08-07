package com.renthouse.tenant.service;

import com.renthouse.tenant.controller.TenantController.Home;
import com.renthouse.tenant.controller.TenantController.Profile;
import com.renthouse.tenant.controller.TenantController.IdentityRequest;
import com.renthouse.tenant.mapper.TenantMapper;
import com.renthouse.tenant.service.TenantService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
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
    @Cacheable(cacheNames = "tenantHome", key = "'home'")
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
            return new Profile(p.nickname(), p.avatarUrl(), p.mobile(), p.realName(), favorites, histories);
        }
        return null;
    }

    @Override
    @Transactional
    public void updateIdentity(long tenantId, IdentityRequest request) {
        mapper.updateUserMobile(tenantId, request.mobile());
        mapper.upsertIdentity(tenantId, request);
    }

    @Override
    public Profile getTenantIdentityForAgent(long agentId, long tenantId) {
        return mapper.getTenantIdentityForAgent(agentId, tenantId);
    }
}
