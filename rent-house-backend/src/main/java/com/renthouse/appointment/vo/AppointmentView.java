package com.renthouse.appointment.vo;
import java.time.LocalDateTime;
public record AppointmentView(String id,String listingId,String listingTitle,String tenantId,String tenantName,String agentId,LocalDateTime scheduledAt,String contactName,String contactMobile,String note,String status,String rejectReason,LocalDateTime createdAt) { }
