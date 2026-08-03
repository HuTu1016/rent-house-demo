package com.renthouse.operation.service.impl;

import com.renthouse.operation.DashboardController.Data;
import com.renthouse.operation.mapper.DashboardMapper;
import com.renthouse.operation.service.DashboardService;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMapper mapper;

    public DashboardServiceImpl(DashboardMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Data getDashboardData(long landlordId) {
        return new Data(
            mapper.countVacantUnits(landlordId),
            mapper.countOccupiedUnits(landlordId),
            mapper.countPendingAppointments(landlordId)
        );
    }
}
