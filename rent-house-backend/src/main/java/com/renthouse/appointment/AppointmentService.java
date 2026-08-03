package com.renthouse.appointment;

import com.renthouse.auth.*;
import com.renthouse.common.api.PageResponse;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.common.id.SnowflakeIdGenerator;
import com.renthouse.conversation.ConversationService;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    private final JdbcTemplate jdbc; private final SnowflakeIdGenerator ids; private final ConversationService conversations;
    public AppointmentService(JdbcTemplate jdbc,SnowflakeIdGenerator ids,ConversationService conversations){this.jdbc=jdbc;this.ids=ids;this.conversations=conversations;}
    @Transactional
    public AppointmentView create(long listingId, CreateCommand cmd){
        CurrentUser.requireRole(UserRole.TENANT); AuthenticatedUser tenant=CurrentUser.require();
        if(!cmd.scheduledAt().isAfter(LocalDateTime.now())) throw new BusinessException("APPOINTMENT_TIME_INVALID","预约时间必须晚于当前时间",HttpStatus.BAD_REQUEST);
        Long landlordId=jdbc.query("SELECT l.landlord_id FROM house_listing l JOIN property_unit u ON u.id=l.unit_id WHERE l.id=? AND l.publish_status='PUBLISHED' AND u.occupancy_status='VACANT'",rs->rs.next()?rs.getLong(1):null,listingId);
        if(landlordId==null) throw new BusinessException("LISTING_NOT_AVAILABLE","房源暂不可预约",HttpStatus.CONFLICT);
        long conversationId=Long.parseLong(conversations.getOrCreateForTenant(listingId).id()); long id=ids.nextId(); LocalDateTime now=LocalDateTime.now();
        jdbc.update("INSERT INTO appointment(id,listing_id,tenant_id,landlord_id,conversation_id,scheduled_at,contact_name,contact_mobile,note,status,created_at,updated_at) VALUES(?,?,?,?,?,?,?,?,?,'PENDING',?,?)",id,listingId,tenant.id(),landlordId,conversationId,cmd.scheduledAt(),cmd.contactName(),cmd.contactMobile(),cmd.note(),now,now);
        jdbc.update("INSERT INTO chat_message(id,conversation_id,sender_id,message_type,content,appointment_id,created_at) VALUES(?,?,?,?,?,?,?)",ids.nextId(),conversationId,tenant.id(),"APPOINTMENT","已提交看房预约",id,now);
        jdbc.update("UPDATE conversation SET last_message_preview=?,last_message_at=?,updated_at=? WHERE id=?","已提交看房预约",now,now,conversationId);
        return find(id);
    }
    public PageResponse<AppointmentView> list(String status,int page,int size){ AuthenticatedUser user=CurrentUser.require(); page=Math.max(1,page);size=Math.min(50,Math.max(1,size));String owner=user.role()==UserRole.LANDLORD?"a.landlord_id":"a.tenant_id";String where=" WHERE "+owner+"=?"+(status==null||status.isBlank()?"":" AND a.status=?"); Object[] p=status==null||status.isBlank()?new Object[]{user.id()}:new Object[]{user.id(),status};Long total=jdbc.queryForObject("SELECT COUNT(*) FROM appointment a"+where,Long.class,p);String sql="SELECT a.*,l.title listing_title,t.nickname tenant_name FROM appointment a JOIN house_listing l ON l.id=a.listing_id JOIN sys_user t ON t.id=a.tenant_id"+where+" ORDER BY a.scheduled_at DESC LIMIT ? OFFSET ?";Object[] p2=status==null||status.isBlank()?new Object[]{user.id(),size,(page-1)*size}:new Object[]{user.id(),status,size,(page-1)*size};return PageResponse.of(jdbc.query(sql,(rs,n)->map(rs),p2),total==null?0:total,page,size); }
    @Transactional
    public AppointmentView updateStatus(long id, AppointmentStatus status,String reason){ AuthenticatedUser user=CurrentUser.require(); AppointmentView view=find(id); AppointmentStatus current=AppointmentStatus.valueOf(view.status());
        boolean landlord=user.role()==UserRole.LANDLORD&&Long.parseLong(view.landlordId())==user.id(); boolean tenant=user.role()==UserRole.TENANT&&Long.parseLong(view.tenantId())==user.id();
        boolean allowed=(landlord&&current==AppointmentStatus.PENDING&&(status==AppointmentStatus.CONFIRMED||status==AppointmentStatus.DECLINED))||(tenant&&current==AppointmentStatus.PENDING&&status==AppointmentStatus.CANCELLED)||(landlord&&current==AppointmentStatus.CONFIRMED&&status==AppointmentStatus.COMPLETED);
        if(!allowed) throw new BusinessException("APPOINTMENT_TRANSITION_INVALID","当前状态不可执行该操作",HttpStatus.CONFLICT);
        jdbc.update("UPDATE appointment SET status=?,reject_reason=?,updated_at=? WHERE id=?",status.name(),status==AppointmentStatus.DECLINED?reason:null,LocalDateTime.now(),id);
        Long conversationId=jdbc.query("SELECT conversation_id FROM appointment WHERE id=?",rs->rs.next()?rs.getLong(1):null,id); if(conversationId!=null){String text="预约状态更新为："+status.name();jdbc.update("INSERT INTO chat_message(id,conversation_id,sender_id,message_type,content,appointment_id,created_at) VALUES(?,?,?,?,?,?,?)",ids.nextId(),conversationId,user.id(),"APPOINTMENT",text,id,LocalDateTime.now());jdbc.update("UPDATE conversation SET last_message_preview=?,last_message_at=?,updated_at=? WHERE id=?",text,LocalDateTime.now(),LocalDateTime.now(),conversationId);}
        return find(id);
    }
    public AppointmentView find(long id){ AuthenticatedUser user=CurrentUser.require();return jdbc.query("SELECT a.*,l.title listing_title,t.nickname tenant_name FROM appointment a JOIN house_listing l ON l.id=a.listing_id JOIN sys_user t ON t.id=a.tenant_id WHERE a.id=? AND (a.tenant_id=? OR a.landlord_id=?)",rs->{if(!rs.next())throw new BusinessException("APPOINTMENT_NOT_FOUND","预约不存在",HttpStatus.NOT_FOUND);return map(rs);},id,user.id(),user.id()); }
    private AppointmentView map(java.sql.ResultSet rs)throws java.sql.SQLException{return new AppointmentView(String.valueOf(rs.getLong("id")),String.valueOf(rs.getLong("listing_id")),rs.getString("listing_title"),String.valueOf(rs.getLong("tenant_id")),rs.getString("tenant_name"),String.valueOf(rs.getLong("landlord_id")),rs.getTimestamp("scheduled_at").toLocalDateTime(),rs.getString("contact_name"),rs.getString("contact_mobile"),rs.getString("note"),rs.getString("status"),rs.getString("reject_reason"),rs.getTimestamp("created_at").toLocalDateTime());}
    public record CreateCommand(LocalDateTime scheduledAt,String contactName,String contactMobile,String note){}
}
