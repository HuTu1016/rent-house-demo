package com.renthouse.operation.service.impl;

import com.renthouse.auth.CurrentUser;
import com.renthouse.auth.UserRole;
import com.renthouse.common.api.PageResponse;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.common.id.SnowflakeIdGenerator;
import com.renthouse.operation.LandlordListingController.Create;
import com.renthouse.operation.LandlordListingController.Item;
import com.renthouse.operation.LandlordListingController.Media;
import com.renthouse.operation.LandlordListingController.Special;
import com.renthouse.operation.mapper.LandlordListingMapper;
import com.renthouse.operation.service.LandlordListingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LandlordListingServiceImpl implements LandlordListingService {
    private final LandlordListingMapper mapper;
    private final SnowflakeIdGenerator ids;

    public LandlordListingServiceImpl(LandlordListingMapper mapper, SnowflakeIdGenerator ids) {
        this.mapper = mapper;
        this.ids = ids;
    }

    private long owner() {
        CurrentUser.requireRole(UserRole.LANDLORD);
        return CurrentUser.require().id();
    }

    @Override
    @Transactional
    public Item create(Create r) {
        long o = owner();
        int ok = mapper.countUnit(r.unitId(), o);
        if (ok == 0) throw new BusinessException("UNIT_NOT_FOUND", "房屋不存在或无权操作", HttpStatus.NOT_FOUND);
        
        long id = ids.nextId();
        LocalDateTime n = LocalDateTime.now();
        mapper.insertListing(id, r.unitId(), o, r.title(), r.communityName(), r.district(), r.address(), r.rentCent(), r.depositCent(), n);
        
        return new Item(String.valueOf(id), r.title(), r.rentCent(), "DRAFT", false, "VACANT");
    }

    @Override
    public PageResponse<Item> list(int page, int size) {
        long o = owner();
        page = Math.max(1, page);
        size = Math.min(50, Math.max(1, size));
        int offset = (page - 1) * size;

        long t = mapper.countListings(o);
        List<Item> items = mapper.listListings(o, offset, size);
        return PageResponse.of(items, t, page, size);
    }

    @Override
    @Transactional
    public void publish(long id) {
        long o = owner();
        int n = mapper.publishListing(id, o, LocalDateTime.now());
        if (n == 0) throw new BusinessException("LISTING_NOT_PUBLISHABLE", "房源不存在、无权操作或当前不可发布", HttpStatus.CONFLICT);
    }

    @Override
    @Transactional
    public void offline(long id) {
        long o = owner();
        int n = mapper.offlineListing(id, o, LocalDateTime.now());
        if (n == 0) throw new BusinessException("LISTING_NOT_FOUND", "房源不存在或无权操作", HttpStatus.NOT_FOUND);
    }

    @Override
    @Transactional
    public void updateSpecial(long id, Special r) {
        long o = owner();
        int n = mapper.updateSpecial(id, o, r.enabled(), r.sort(), LocalDateTime.now());
        if (n == 0) throw new BusinessException("LISTING_NOT_FOUND", "房源不存在或无权操作", HttpStatus.NOT_FOUND);
    }

    @Override
    @Transactional
    public void addMedia(long id, Media r) {
        long o = owner();
        int n = mapper.countListing(id, o);
        if (n == 0) throw new BusinessException("LISTING_NOT_FOUND", "房源不存在或无权操作", HttpStatus.NOT_FOUND);
        mapper.insertMedia(ids.nextId(), id, r.type(), r.url(), r.coverUrl(), r.sort(), LocalDateTime.now());
    }
}
