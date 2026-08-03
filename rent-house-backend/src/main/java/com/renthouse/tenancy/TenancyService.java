package com.renthouse.tenancy;
import com.renthouse.appointment.AppointmentStatus;
import com.renthouse.auth.*;
import com.renthouse.common.api.PageResponse;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.common.id.SnowflakeIdGenerator;
import java.time.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class TenancyService {
 private final JdbcTemplate jdbc; private final SnowflakeIdGenerator ids;
 public TenancyService(JdbcTemplate jdbc,SnowflakeIdGenerator ids){this.jdbc=jdbc;this.ids=ids;}
 @Transactional public ContractView createFromAppointment(long appointmentId,CreateCommand c){
  CurrentUser.requireRole(UserRole.LANDLORD); long landlord=CurrentUser.require().id();
  var a=jdbc.query("SELECT a.listing_id,a.tenant_id,l.unit_id,l.rent_cent,l.deposit_cent FROM appointment a JOIN house_listing l ON l.id=a.listing_id WHERE a.id=? AND a.landlord_id=? AND a.status='COMPLETED'",rs->rs.next()?new long[]{rs.getLong(1),rs.getLong(2),rs.getLong(3),rs.getLong(4),rs.getLong(5)}:null,appointmentId,landlord);
  if(a==null)throw new BusinessException("APPOINTMENT_NOT_ELIGIBLE","仅已完成的本房东预约可生成合同",HttpStatus.CONFLICT);
  if(!c.endDate().isAfter(c.startDate()))throw new BusinessException("CONTRACT_DATE_INVALID","合同结束日期须晚于开始日期",HttpStatus.BAD_REQUEST);
  Integer vacant=jdbc.queryForObject("SELECT COUNT(*) FROM property_unit WHERE id=? AND occupancy_status='VACANT'",Integer.class,a[2]);if(vacant==null||vacant==0)throw new BusinessException("UNIT_NOT_VACANT","房源当前不可签约",HttpStatus.CONFLICT);
  long id=ids.nextId();LocalDateTime now=LocalDateTime.now();LocalDateTime expiry=now.plusHours(48);String no="HT"+id;
  jdbc.update("INSERT INTO tenancy_contract(id,contract_no,listing_id,unit_id,tenant_id,landlord_id,appointment_id,start_date,end_date,rent_cent,deposit_cent,payment_day,status,draft_expire_at,created_at,updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?, 'DRAFT',?,?,?)",id,no,a[0],a[2],a[1],landlord,appointmentId,c.startDate(),c.endDate(),c.rentCent()==null?a[3]:c.rentCent(),c.depositCent()==null?a[4]:c.depositCent(),c.paymentDay(),expiry,now,now);
  jdbc.update("UPDATE property_unit SET occupancy_status='PENDING_SIGN',updated_at=? WHERE id=?",now,a[2]);jdbc.update("UPDATE appointment SET status='CONVERTED',updated_at=? WHERE id=?",now,appointmentId);return find(id);
 }
 @Transactional public ContractView sign(long id){CurrentUser.requireRole(UserRole.TENANT);ContractView v=find(id);if(Long.parseLong(v.tenantId())!=CurrentUser.require().id()||!"DRAFT".equals(v.status())||v.draftExpireAt().isBefore(LocalDateTime.now()))throw new BusinessException("CONTRACT_NOT_SIGNABLE","合同当前不可签署",HttpStatus.CONFLICT);LocalDateTime now=LocalDateTime.now();jdbc.update("UPDATE tenancy_contract SET status='ACTIVE',signed_at=?,updated_at=? WHERE id=?",now,now,id);Long unit=jdbc.queryForObject("SELECT unit_id FROM tenancy_contract WHERE id=?",Long.class,id);jdbc.update("UPDATE property_unit SET occupancy_status='OCCUPIED',updated_at=? WHERE id=?",now,unit);return find(id);}
 @Transactional public ContractView checkout(long id){CurrentUser.requireRole(UserRole.TENANT);ContractView v=find(id);if(Long.parseLong(v.tenantId())!=CurrentUser.require().id()||!"ACTIVE".equals(v.status()))throw new BusinessException("CHECKOUT_NOT_ALLOWED","当前合同不可申请退租",HttpStatus.CONFLICT);LocalDateTime now=LocalDateTime.now();jdbc.update("UPDATE tenancy_contract SET status='CHECKOUT_PENDING',checkout_apply_at=?,updated_at=? WHERE id=?",now,now,id);Long unit=jdbc.queryForObject("SELECT unit_id FROM tenancy_contract WHERE id=?",Long.class,id);jdbc.update("UPDATE property_unit SET occupancy_status='CHECKOUT_PENDING',updated_at=? WHERE id=?",now,unit);return find(id);}
 public PageResponse<ContractView> list(int page,int size){AuthenticatedUser u=CurrentUser.require();page=Math.max(1,page);size=Math.min(50,Math.max(1,size));String col=u.role()==UserRole.LANDLORD?"landlord_id":"tenant_id";Long total=jdbc.queryForObject("SELECT COUNT(*) FROM tenancy_contract WHERE "+col+"=?",Long.class,u.id());return PageResponse.of(jdbc.query("SELECT * FROM tenancy_contract WHERE "+col+"=? ORDER BY created_at DESC LIMIT ? OFFSET ?",(rs,n)->map(rs),u.id(),size,(page-1)*size),total==null?0:total,page,size);}
 public ContractView find(long id){AuthenticatedUser u=CurrentUser.require();return jdbc.query("SELECT * FROM tenancy_contract WHERE id=? AND (tenant_id=? OR landlord_id=?)",rs->{if(!rs.next())throw new BusinessException("CONTRACT_NOT_FOUND","合同不存在",HttpStatus.NOT_FOUND);return map(rs);},id,u.id(),u.id());}
 private ContractView map(java.sql.ResultSet r)throws java.sql.SQLException{return new ContractView(""+r.getLong("id"),r.getString("contract_no"),""+r.getLong("listing_id"),""+r.getLong("tenant_id"),""+r.getLong("landlord_id"),r.getDate("start_date").toLocalDate(),r.getDate("end_date").toLocalDate(),r.getInt("rent_cent"),r.getInt("deposit_cent"),r.getInt("payment_day"),r.getString("status"),r.getTimestamp("draft_expire_at")==null?null:r.getTimestamp("draft_expire_at").toLocalDateTime(),r.getTimestamp("signed_at")==null?null:r.getTimestamp("signed_at").toLocalDateTime(),r.getTimestamp("checkout_apply_at")==null?null:r.getTimestamp("checkout_apply_at").toLocalDateTime());}
 public record CreateCommand(LocalDate startDate,LocalDate endDate,Integer rentCent,Integer depositCent,int paymentDay){}
}
