package com.renthouse.operation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DashboardMapper {
    long countVacantUnits(@Param("landlordId") long landlordId);
    long countOccupiedUnits(@Param("landlordId") long landlordId);
    long countPendingAppointments(@Param("landlordId") long landlordId);
}
