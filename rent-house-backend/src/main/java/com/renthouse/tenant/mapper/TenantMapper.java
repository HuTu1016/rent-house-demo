package com.renthouse.tenant.mapper;

import com.renthouse.tenant.controller.TenantController.Profile;
import com.renthouse.tenant.controller.TenantController.Special;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TenantMapper {
    List<Special> getSpecials();
    long countPublishedListings();
    long countVacantUnits();
    Profile getUserProfile(@Param("id") long id);
    long countFavorites(@Param("tenantId") long tenantId);
    long countHistories(@Param("tenantId") long tenantId);
    void updateUserMobile(@Param("tenantId") long tenantId, @Param("mobile") String mobile);
    void upsertIdentity(@Param("tenantId") long tenantId, @Param("request") com.renthouse.tenant.controller.TenantController.IdentityRequest request);
    Profile getTenantIdentityForAgent(@Param("agentId") long agentId, @Param("tenantId") long tenantId);
}
