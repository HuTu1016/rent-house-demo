package com.renthouse.operation.service.impl;

import com.renthouse.auth.CurrentUser;
import com.renthouse.auth.UserRole;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.common.id.SnowflakeIdGenerator;
import com.renthouse.operation.PropertyController.Building;
import com.renthouse.operation.PropertyController.Unit;
import com.renthouse.operation.mapper.PropertyMapper;
import com.renthouse.operation.service.PropertyService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PropertyServiceImpl implements PropertyService {
    private final PropertyMapper mapper;
    private final SnowflakeIdGenerator ids;

    public PropertyServiceImpl(PropertyMapper mapper, SnowflakeIdGenerator ids) {
        this.mapper = mapper;
        this.ids = ids;
    }

    @Override
    @Transactional
    public String createBuilding(Building r) {
        CurrentUser.requireRole(UserRole.AGENT);
        long id = ids.nextId();
        mapper.insertBuilding(id, CurrentUser.require().id(), r.name(), r.address(), LocalDateTime.now());
        return String.valueOf(id);
    }

    @Override
    @Transactional
    public String createUnit(Unit r) {
        CurrentUser.requireRole(UserRole.AGENT);
        long owner = CurrentUser.require().id();
        int exists = mapper.countBuilding(r.buildingId(), owner);
        if (exists == 0) {
            throw new BusinessException("BUILDING_NOT_FOUND", "楼盘不存在或无权操作", HttpStatus.NOT_FOUND);
        }
        long id = ids.nextId();
        mapper.insertUnit(id, r.buildingId(), owner, r.unitNo(), r.title(), r.rooms(), r.halls(), r.bathrooms(), r.areaSqm(), LocalDateTime.now());
        return String.valueOf(id);
    }
}
