package com.renthouse.repair;
import java.time.*;public record RepairView(String id,String ticketNo,String contractId,String tenantId,String landlordId,String category,String description,String status,String assigneeName,String assigneeMobile,String handlingNote,LocalDateTime completedAt,LocalDateTime createdAt){}
