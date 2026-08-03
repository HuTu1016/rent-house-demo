package com.renthouse.listing.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renthouse.auth.service.CurrentUser;
import com.renthouse.auth.enums.UserRole;
import com.renthouse.common.api.PageResponse;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.common.id.SnowflakeIdGenerator;
import com.renthouse.listing.mapper.ListingMapper;
import com.renthouse.listing.vo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ListingService {
    private final ListingMapper mapper;
    private final ObjectMapper objectMapper;
    private final SnowflakeIdGenerator idGenerator;

    public ListingService(ListingMapper mapper, ObjectMapper objectMapper, SnowflakeIdGenerator idGenerator) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.idGenerator = idGenerator;
    }

    public PageResponse<ListingView> search(SearchQuery query) {
        Long tenantId = optionalTenantId();
        String orderBy = switch (query.sort()) { case "rentAsc" -> "l.rent_cent ASC"; case "rentDesc" -> "l.rent_cent DESC"; case "areaDesc" -> "u.area_sqm DESC"; default -> "l.is_special DESC,l.special_sort ASC,l.published_at DESC"; };
        int page = Math.max(1, query.page());
        int size = Math.min(50, Math.max(1, query.size()));
        int offset = (page - 1) * size;

        long count = mapper.countListings(query);
        List<ListingView> result = mapper.searchListings(query, tenantId, offset, size, orderBy);
        return PageResponse.of(result, count, page, size);
    }

    @Transactional
    public ListingDetailView detail(long listingId) {
        Long tenantId = optionalTenantId();
        Map<String, Object> row = mapper.getListingDetail(listingId, tenantId);
        if (row == null || row.isEmpty()) throw new BusinessException("LISTING_NOT_FOUND", "房源不存在或已下架", HttpStatus.NOT_FOUND);
        
        if (tenantId != null) {
            mapper.upsertHistory(idGenerator.nextId(), tenantId, listingId, LocalDateTime.now());
        }
        
        List<ListingDetailView.MediaView> media = mapper.getListingMedia(listingId);
        
        ListingView listing = new ListingView(
            String.valueOf(row.get("id")), (String)row.get("title"), (String)row.get("community_name"), (String)row.get("district"),
            (String)row.get("address"), ((Number)row.get("rent_cent")).intValue(), ((Number)row.get("deposit_cent")).intValue(),
            ((Number)row.get("room_count")).intValue(), ((Number)row.get("hall_count")).intValue(), ((Number)row.get("bathroom_count")).intValue(),
            ((Number)row.get("area_sqm")).doubleValue(), (String)row.get("orientation"), jsonList((String)row.get("tags_json")),
            (String)row.get("cover_url"), ((Number)row.get("favorite")).intValue() > 0, (Boolean)row.get("is_special")
        );
        return new ListingDetailView(listing, (String)row.get("description"), jsonList((String)row.get("facilities_json")), media);
    }

    @Transactional
    public void favorite(long listingId) { setFavorite(listingId, true); }
    @Transactional
    public void unfavorite(long listingId) { setFavorite(listingId, false); }
    
    public PageResponse<ListingView> favorites(int page, int size) { return byRelation("tenant_favorite", "created_at", page, size); }
    public PageResponse<ListingView> history(int page, int size) { return byRelation("tenant_browse_history", "viewed_at", page, size); }
    
    private PageResponse<ListingView> byRelation(String table, String timeColumn, int page, int size) {
        CurrentUser.requireRole(UserRole.TENANT);
        long tenantId = CurrentUser.require().id();
        page = Math.max(1, page);
        size = Math.min(50, Math.max(1, size));
        int offset = (page - 1) * size;
        
        long total = mapper.countHistoryOrFavorite(table, tenantId);
        List<ListingView> list = mapper.listHistoryOrFavorite(table, timeColumn, tenantId, offset, size);
        return PageResponse.of(list, total, page, size);
    }

    private void setFavorite(long listingId, boolean enabled) {
        CurrentUser.requireRole(UserRole.TENANT);
        long tenantId = CurrentUser.require().id();
        if (enabled) {
            if (mapper.countPublishedListing(listingId) == 0) throw new BusinessException("LISTING_NOT_FOUND", "房源不存在", HttpStatus.NOT_FOUND);
            mapper.insertFavorite(tenantId, listingId, LocalDateTime.now());
        } else {
            mapper.deleteFavorite(tenantId, listingId);
        }
    }

    private Long optionalTenantId() { try { return CurrentUser.require().role() == UserRole.TENANT ? CurrentUser.require().id() : null; } catch (BusinessException ignored) { return null; } }
    
    private List<String> jsonList(String json) { try { return json == null ? List.of() : objectMapper.readValue(json, new TypeReference<>() { }); } catch (Exception ignored) { return List.of(); } }
    
    public record SearchQuery(String keyword, String district, Integer minRentCent, Integer maxRentCent, Integer rooms, String sort, int page, int size) { }
}
