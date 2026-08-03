package com.renthouse.conversation;

import com.renthouse.auth.*;
import com.renthouse.common.api.PageResponse;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.common.id.SnowflakeIdGenerator;
import com.renthouse.conversation.mapper.ConversationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConversationService {
    private final ConversationMapper mapper;
    private final AppUserMapper users;
    private final SnowflakeIdGenerator ids;

    public ConversationService(ConversationMapper mapper, AppUserMapper users, SnowflakeIdGenerator ids) {
        this.mapper = mapper;
        this.users = users;
        this.ids = ids;
    }

    @Transactional
    public ConversationView getOrCreateForTenant(long listingId) {
        CurrentUser.requireRole(UserRole.TENANT);
        long tenantId = CurrentUser.require().id();
        AppUser tenant = java.util.Optional.ofNullable(users.selectById(tenantId)).orElseThrow();
        if (tenant.getStatus() != UserStatus.ACTIVE) throw new BusinessException("CHAT_BLOCKED", "当前账号无法发起会话", HttpStatus.FORBIDDEN);

        Long landlordId = mapper.getLandlordIdByListing(listingId);
        if (landlordId == null) throw new BusinessException("LISTING_NOT_FOUND", "房源不可联系", HttpStatus.NOT_FOUND);

        Long conversationId = mapper.getConversationIdByListingAndUsers(listingId, tenantId, landlordId);
        if (conversationId == null) {
            conversationId = ids.nextId();
            LocalDateTime now = LocalDateTime.now();
            mapper.insertConversation(conversationId, listingId, tenantId, landlordId, now);
        }
        return mapper.findConversationView(conversationId, tenantId);
    }

    public PageResponse<ConversationView> list(int page, int size) {
        AuthenticatedUser user = CurrentUser.require();
        page = Math.max(1, page);
        size = Math.min(50, Math.max(1, size));
        int offset = (page - 1) * size;
        String ownerColumn = user.role() == UserRole.LANDLORD ? "c.landlord_id" : "c.tenant_id";

        long total = mapper.countConversations(ownerColumn, user.id());
        List<ConversationView> list = mapper.listConversations(ownerColumn, user.id(), offset, size);

        return PageResponse.of(list, total, page, size);
    }

    @Transactional
    public PageResponse<MessageView> messages(long conversationId, int page, int size) {
        ensureParticipant(conversationId);
        AuthenticatedUser user = CurrentUser.require();
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        int offset = (page - 1) * size;

        mapper.updateMessagesRead(conversationId, user.id(), LocalDateTime.now());
        long total = mapper.countMessages(conversationId);
        
        List<Map<String, Object>> rows = mapper.listMessages(conversationId, offset, size);
        List<MessageView> result = rows.stream().map(rs -> new MessageView(
                String.valueOf(rs.get("id")),
                String.valueOf(rs.get("sender_id")),
                (String) rs.get("message_type"),
                (String) rs.get("content"),
                rs.get("appointment_id") == null ? null : String.valueOf(rs.get("appointment_id")),
                rs.get("created_at") instanceof java.sql.Timestamp ? ((java.sql.Timestamp)rs.get("created_at")).toLocalDateTime() : (LocalDateTime) rs.get("created_at"),
                ((Number)rs.get("sender_id")).longValue() == user.id()
        )).collect(Collectors.toList());

        return PageResponse.of(result, total, page, size);
    }

    @Transactional
    public MessageView send(long conversationId, String content) {
        ensureParticipant(conversationId);
        if (content == null || content.isBlank() || content.length() > 1000)
            throw new BusinessException("MESSAGE_INVALID", "消息长度需为 1-1000 字", HttpStatus.BAD_REQUEST);

        AuthenticatedUser user = CurrentUser.require();
        long id = ids.nextId();
        LocalDateTime now = LocalDateTime.now();
        String trimmed = content.trim();

        mapper.insertMessage(id, conversationId, user.id(), "TEXT", trimmed, now);
        
        String preview = trimmed.substring(0, Math.min(trimmed.length(), 255));
        mapper.updateConversationPreview(conversationId, preview, now);
        
        return new MessageView(String.valueOf(id), String.valueOf(user.id()), "TEXT", trimmed, null, now, true);
    }

    public void ensureParticipant(long conversationId) {
        AuthenticatedUser user = CurrentUser.require();
        int hit = mapper.countParticipant(conversationId, user.id());
        if (hit == 0) throw new BusinessException("CONVERSATION_FORBIDDEN", "无权访问此会话", HttpStatus.FORBIDDEN);
    }
}
