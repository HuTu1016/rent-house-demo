package com.renthouse.operation.service.impl;

import com.renthouse.auth.CurrentUser;
import com.renthouse.auth.UserRole;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.operation.BlacklistController.Request;
import com.renthouse.operation.mapper.BlacklistMapper;
import com.renthouse.operation.service.BlacklistService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BlacklistServiceImpl implements BlacklistService {

    private final BlacklistMapper mapper;

    public BlacklistServiceImpl(BlacklistMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public void block(long tenantId, Request r) {
        CurrentUser.requireRole(UserRole.LANDLORD);
        int exists = mapper.countAppointment(CurrentUser.require().id(), tenantId);
        if (exists == 0) throw new BusinessException("TENANT_NOT_FOUND", "该租客不属于当前中介(无带看预约记录)", HttpStatus.NOT_FOUND);
        
        LocalDateTime now = LocalDateTime.now();
        mapper.blockUser(tenantId, now);
        mapper.blockProfile(tenantId, r.reason(), now);
    }

    @Override
    @Transactional
    public void unblock(long tenantId) {
        CurrentUser.requireRole(UserRole.LANDLORD);
        LocalDateTime now = LocalDateTime.now();
        mapper.unblockUser(tenantId, now);
        mapper.unblockProfile(tenantId, now);
    }
}
