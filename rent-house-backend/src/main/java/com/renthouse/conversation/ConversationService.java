package com.renthouse.conversation;

import com.renthouse.auth.*;
import com.renthouse.common.api.PageResponse;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.common.id.SnowflakeIdGenerator;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConversationService {
    private final JdbcTemplate jdbc;
    private final AppUserRepository users;
    private final SnowflakeIdGenerator ids;
    public ConversationService(JdbcTemplate jdbc, AppUserRepository users, SnowflakeIdGenerator ids) { this.jdbc = jdbc; this.users = users; this.ids = ids; }
    @Transactional
    public ConversationView getOrCreateForTenant(long listingId) {
        CurrentUser.requireRole(UserRole.TENANT); long tenantId = CurrentUser.require().id();
        AppUser tenant = users.findById(tenantId).orElseThrow();
        if (tenant.getStatus() != UserStatus.ACTIVE) throw new BusinessException("CHAT_BLOCKED", "当前账号无法发起会话", HttpStatus.FORBIDDEN);
        Long landlordId = jdbc.query("SELECT landlord_id FROM house_listing l JOIN property_unit u ON u.id=l.unit_id WHERE l.id=? AND l.publish_status='PUBLISHED' AND u.occupancy_status='VACANT'", rs -> rs.next() ? rs.getLong(1) : null, listingId);
        if (landlordId == null) throw new BusinessException("LISTING_NOT_FOUND", "房源不可联系", HttpStatus.NOT_FOUND);
        Long conversationId = jdbc.query("SELECT id FROM conversation WHERE listing_id=? AND tenant_id=? AND landlord_id=?", rs -> rs.next() ? rs.getLong(1) : null, listingId, tenantId, landlordId);
        if (conversationId == null) { conversationId = ids.nextId(); LocalDateTime now = LocalDateTime.now(); jdbc.update("INSERT INTO conversation(id,listing_id,tenant_id,landlord_id,created_at,updated_at) VALUES(?,?,?,?,?,?)", conversationId, listingId, tenantId, landlordId, now, now); }
        return findView(conversationId, tenantId);
    }
    public PageResponse<ConversationView> list(int page, int size) {
        AuthenticatedUser user = CurrentUser.require(); page = Math.max(1,page); size = Math.min(50,Math.max(1,size));
        String ownerColumn = user.role() == UserRole.LANDLORD ? "c.landlord_id" : "c.tenant_id";
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM conversation c WHERE " + ownerColumn + "=?", Long.class, user.id());
        String sql = "SELECT c.id,c.listing_id,l.title listing_title,c.tenant_id,c.landlord_id,c.last_message_preview,c.last_message_at," +
                "CASE WHEN c.tenant_id=? THEN lt.id ELSE tt.id END peer_id, CASE WHEN c.tenant_id=? THEN lt.nickname ELSE tt.nickname END peer_name," +
                "(SELECT COUNT(*) FROM chat_message m WHERE m.conversation_id=c.id AND m.sender_id<>? AND m.read_at IS NULL) unread_count " +
                "FROM conversation c JOIN house_listing l ON l.id=c.listing_id JOIN sys_user tt ON tt.id=c.tenant_id JOIN sys_user lt ON lt.id=c.landlord_id WHERE " + ownerColumn + "=? ORDER BY c.last_message_at DESC,c.created_at DESC LIMIT ? OFFSET ?";
        return PageResponse.of(jdbc.query(sql, (rs,n)->new ConversationView(String.valueOf(rs.getLong("id")),String.valueOf(rs.getLong("listing_id")),rs.getString("listing_title"),String.valueOf(rs.getLong("peer_id")),rs.getString("peer_name"),rs.getString("last_message_preview"),rs.getTimestamp("last_message_at")==null?null:rs.getTimestamp("last_message_at").toLocalDateTime(),rs.getLong("unread_count")),user.id(),user.id(),user.id(),user.id(),size,(page-1)*size),total == null ? 0 : total,page,size);
    }
    @Transactional
    public PageResponse<MessageView> messages(long conversationId, int page, int size) {
        ensureParticipant(conversationId); AuthenticatedUser user = CurrentUser.require(); page=Math.max(1,page); size=Math.min(100,Math.max(1,size));
        jdbc.update("UPDATE chat_message SET read_at=? WHERE conversation_id=? AND sender_id<>? AND read_at IS NULL",LocalDateTime.now(),conversationId,user.id());
        Long total=jdbc.queryForObject("SELECT COUNT(*) FROM chat_message WHERE conversation_id=?",Long.class,conversationId);
        return PageResponse.of(jdbc.query("SELECT id,sender_id,message_type,content,appointment_id,created_at FROM chat_message WHERE conversation_id=? ORDER BY id DESC LIMIT ? OFFSET ?",(rs,n)->new MessageView(String.valueOf(rs.getLong("id")),String.valueOf(rs.getLong("sender_id")),rs.getString("message_type"),rs.getString("content"),rs.getObject("appointment_id")==null?null:String.valueOf(rs.getLong("appointment_id")),rs.getTimestamp("created_at").toLocalDateTime(),rs.getLong("sender_id")==user.id()),conversationId,size,(page-1)*size),total==null?0:total,page,size);
    }
    @Transactional
    public MessageView send(long conversationId, String content) {
        ensureParticipant(conversationId); if (content == null || content.isBlank() || content.length() > 1000) throw new BusinessException("MESSAGE_INVALID","消息长度需为 1-1000 字",HttpStatus.BAD_REQUEST);
        AuthenticatedUser user=CurrentUser.require(); long id=ids.nextId(); LocalDateTime now=LocalDateTime.now(); jdbc.update("INSERT INTO chat_message(id,conversation_id,sender_id,message_type,content,created_at) VALUES(?,?,?,?,?,?)",id,conversationId,user.id(),"TEXT",content.trim(),now); jdbc.update("UPDATE conversation SET last_message_preview=?,last_message_at=?,updated_at=? WHERE id=?",content.trim().substring(0,Math.min(content.trim().length(),255)),now,now,conversationId); return new MessageView(String.valueOf(id),String.valueOf(user.id()),"TEXT",content.trim(),null,now,true);
    }
    public void ensureParticipant(long conversationId) { AuthenticatedUser user=CurrentUser.require(); Integer hit=jdbc.queryForObject("SELECT COUNT(*) FROM conversation WHERE id=? AND (tenant_id=? OR landlord_id=?)",Integer.class,conversationId,user.id(),user.id()); if(hit==null||hit==0) throw new BusinessException("CONVERSATION_FORBIDDEN","无权访问此会话",HttpStatus.FORBIDDEN); }
    private ConversationView findView(long conversationId,long userId) { return jdbc.query("SELECT c.id,c.listing_id,l.title listing_title,CASE WHEN c.tenant_id=? THEN lt.id ELSE tt.id END peer_id,CASE WHEN c.tenant_id=? THEN lt.nickname ELSE tt.nickname END peer_name,c.last_message_preview,c.last_message_at,0 unread_count FROM conversation c JOIN house_listing l ON l.id=c.listing_id JOIN sys_user tt ON tt.id=c.tenant_id JOIN sys_user lt ON lt.id=c.landlord_id WHERE c.id=?",rs->rs.next()?new ConversationView(String.valueOf(rs.getLong("id")),String.valueOf(rs.getLong("listing_id")),rs.getString("listing_title"),String.valueOf(rs.getLong("peer_id")),rs.getString("peer_name"),rs.getString("last_message_preview"),rs.getTimestamp("last_message_at")==null?null:rs.getTimestamp("last_message_at").toLocalDateTime(),0):null,userId,userId,conversationId); }
}
