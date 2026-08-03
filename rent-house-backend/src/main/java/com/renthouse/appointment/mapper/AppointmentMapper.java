package com.renthouse.appointment.mapper;

import com.renthouse.appointment.vo.AppointmentView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AppointmentMapper {

    Long getAgentIdByListing(@Param("listingId") long listingId);

    void insertAppointment(
            @Param("id") long id, @Param("listingId") long listingId, @Param("tenantId") long tenantId,
            @Param("agentId") long agentId, @Param("conversationId") long conversationId,
            @Param("scheduledAt") LocalDateTime scheduledAt, @Param("contactName") String contactName,
            @Param("contactMobile") String contactMobile, @Param("note") String note,
            @Param("status") String status, @Param("now") LocalDateTime now
    );

    void insertChatMessage(
            @Param("id") long id, @Param("conversationId") long conversationId, @Param("senderId") long senderId,
            @Param("messageType") String messageType, @Param("content") String content,
            @Param("appointmentId") long appointmentId, @Param("now") LocalDateTime now
    );

    void updateConversationMessage(
            @Param("conversationId") long conversationId, @Param("content") String content, @Param("now") LocalDateTime now
    );

    long countAppointments(@Param("ownerColumn") String ownerColumn, @Param("userId") long userId, @Param("status") String status);

    List<AppointmentView> listAppointments(
            @Param("ownerColumn") String ownerColumn, @Param("userId") long userId,
            @Param("status") String status, @Param("offset") int offset, @Param("size") int size
    );

    AppointmentView findAppointment(@Param("id") long id, @Param("userId") long userId);

    void updateAppointmentStatus(
            @Param("id") long id, @Param("status") String status, @Param("reason") String reason, @Param("now") LocalDateTime now
    );

    Long getConversationIdByAppointment(@Param("id") long id);
}
