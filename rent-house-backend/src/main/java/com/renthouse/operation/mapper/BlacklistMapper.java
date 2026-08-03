package com.renthouse.operation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface BlacklistMapper {

    int countAppointment(@Param("landlordId") long landlordId, @Param("tenantId") long tenantId);

    void blockUser(@Param("tenantId") long tenantId, @Param("now") LocalDateTime now);

    void blockProfile(@Param("tenantId") long tenantId, @Param("reason") String reason, @Param("now") LocalDateTime now);

    void unblockUser(@Param("tenantId") long tenantId, @Param("now") LocalDateTime now);

    void unblockProfile(@Param("tenantId") long tenantId, @Param("now") LocalDateTime now);
}
