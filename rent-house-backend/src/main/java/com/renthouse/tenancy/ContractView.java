package com.renthouse.tenancy;
import java.time.*;
public record ContractView(String id,String contractNo,String listingId,String tenantId,String landlordId,LocalDate startDate,LocalDate endDate,int rentCent,int depositCent,int paymentDay,String status,LocalDateTime draftExpireAt,LocalDateTime signedAt,LocalDateTime checkoutApplyAt){}
