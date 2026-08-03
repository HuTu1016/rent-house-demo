package com.renthouse.appointment.service;

import com.renthouse.appointment.mapper.AppointmentMapper;
import com.renthouse.appointment.enums.AppointmentStatus;
import com.renthouse.appointment.vo.AppointmentView;
import com.renthouse.auth.entity.*; import com.renthouse.auth.enums.*; import com.renthouse.auth.mapper.*; import com.renthouse.auth.service.*;
import com.renthouse.common.api.PageResponse;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.common.id.SnowflakeIdGenerator;
import com.renthouse.conversation.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {
    private final AppointmentMapper mapper;
    private final SnowflakeIdGenerator ids;
    private final ConversationService conversations;

    public AppointmentService(AppointmentMapper mapper, SnowflakeIdGenerator ids, ConversationService conversations) {
        this.mapper = mapper;
        this.ids = ids;
        this.conversations = conversations;
    }

    @Transactional
    public AppointmentView create(long listingId, CreateCommand cmd) {
        CurrentUser.requireRole(UserRole.TENANT);
        AuthenticatedUser tenant = CurrentUser.require();
        if (!cmd.scheduledAt().isAfter(LocalDateTime.now()))
            throw new BusinessException("APPOINTMENT_TIME_INVALID", "预约时间必须晚于当前时间", HttpStatus.BAD_REQUEST);

        Long agentId = mapper.getAgentIdByListing(listingId);
        if (agentId == null) throw new BusinessException("LISTING_NOT_AVAILABLE", "房源暂不可预约", HttpStatus.CONFLICT);

        long conversationId = Long.parseLong(conversations.getOrCreateForTenant(listingId).id());
        long id = ids.nextId();
        LocalDateTime now = LocalDateTime.now();

        mapper.insertAppointment(id, listingId, tenant.id(), agentId, conversationId, cmd.scheduledAt(), cmd.contactName(), cmd.contactMobile(), cmd.note(), "PENDING", now);
        
        String text = "已提交看房预约";
        mapper.insertChatMessage(ids.nextId(), conversationId, tenant.id(), "APPOINTMENT", text, id, now);
        mapper.updateConversationMessage(conversationId, text, now);

        return find(id);
    }

    public PageResponse<AppointmentView> list(String status, int page, int size) {
        AuthenticatedUser user = CurrentUser.require();
        page = Math.max(1, page);
        size = Math.min(50, Math.max(1, size));
        int offset = (page - 1) * size;

        String ownerColumn = user.role() == UserRole.AGENT ? "agent_id" : "tenant_id";
        long total = mapper.countAppointments(ownerColumn, user.id(), status);
        List<AppointmentView> result = mapper.listAppointments(ownerColumn, user.id(), status, offset, size);

        return PageResponse.of(result, total, page, size);
    }

    @Transactional
    public AppointmentView updateStatus(long id, AppointmentStatus status, String reason) {
        AuthenticatedUser user = CurrentUser.require();
        AppointmentView view = find(id);
        AppointmentStatus current = AppointmentStatus.valueOf(view.status());

        boolean agent = user.role() == UserRole.AGENT && Long.parseLong(view.agentId()) == user.id();
        boolean tenant = user.role() == UserRole.TENANT && Long.parseLong(view.tenantId()) == user.id();

        boolean allowed = (agent && current == AppointmentStatus.PENDING && (status == AppointmentStatus.CONFIRMED || status == AppointmentStatus.DECLINED)) ||
                (tenant && current == AppointmentStatus.PENDING && status == AppointmentStatus.CANCELLED) ||
                (agent && current == AppointmentStatus.CONFIRMED && status == AppointmentStatus.COMPLETED);

        if (!allowed) throw new BusinessException("APPOINTMENT_TRANSITION_INVALID", "当前状态不可执行该操作", HttpStatus.CONFLICT);

        LocalDateTime now = LocalDateTime.now();
        mapper.updateAppointmentStatus(id, status.name(), status == AppointmentStatus.DECLINED ? reason : null, now);

        Long conversationId = mapper.getConversationIdByAppointment(id);
        if (conversationId != null) {
            String text = "预约状态更新为：" + status.name();
            mapper.insertChatMessage(ids.nextId(), conversationId, user.id(), "APPOINTMENT", text, id, now);
            mapper.updateConversationMessage(conversationId, text, now);
        }

        return find(id);
    }

    public AppointmentView find(long id) {
        AuthenticatedUser user = CurrentUser.require();
        AppointmentView view = mapper.findAppointment(id, user.id());
        if (view == null) throw new BusinessException("APPOINTMENT_NOT_FOUND", "预约不存在", HttpStatus.NOT_FOUND);
        return view;
    }

    public record CreateCommand(LocalDateTime scheduledAt, String contactName, String contactMobile, String note) {}
}
