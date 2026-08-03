package com.renthouse.operation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PropertyMapper {
    void insertBuilding(@Param("id") long id, @Param("agentId") long agentId, @Param("name") String name, @Param("address") String address, @Param("now") LocalDateTime now);
    
    int countBuilding(@Param("id") long id, @Param("agentId") long agentId);
    
    void insertUnit(@Param("id") long id, @Param("buildingId") long buildingId, @Param("agentId") long agentId, 
                    @Param("unitNo") String unitNo, @Param("title") String title, @Param("rooms") int rooms, 
                    @Param("halls") int halls, @Param("bathrooms") int bathrooms, @Param("areaSqm") double areaSqm, 
                    @Param("now") LocalDateTime now);
}
