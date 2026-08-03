package com.renthouse.listing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renthouse.auth.CurrentUser;
import com.renthouse.auth.UserRole;
import com.renthouse.common.api.PageResponse;
import com.renthouse.common.exception.BusinessException;
import com.renthouse.common.id.SnowflakeIdGenerator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListingService {
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final SnowflakeIdGenerator idGenerator;
    public ListingService(JdbcTemplate jdbc, ObjectMapper objectMapper, SnowflakeIdGenerator idGenerator) { this.jdbc = jdbc; this.objectMapper = objectMapper; this.idGenerator = idGenerator; }

    public PageResponse<ListingView> search(SearchQuery query) {
        Long tenantId = optionalTenantId();
        List<Object> parameters = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE l.publish_status = 'PUBLISHED' AND u.occupancy_status = 'VACANT'");
        if (query.district() != null && !query.district().isBlank()) { where.append(" AND l.district = ?"); parameters.add(query.district()); }
        if (query.keyword() != null && !query.keyword().isBlank()) { where.append(" AND (l.title LIKE ? OR l.community_name LIKE ? OR l.address LIKE ?)"); String keyword = "%" + query.keyword().trim() + "%"; parameters.add(keyword); parameters.add(keyword); parameters.add(keyword); }
        if (query.minRentCent() != null) { where.append(" AND l.rent_cent >= ?"); parameters.add(query.minRentCent()); }
        if (query.maxRentCent() != null) { where.append(" AND l.rent_cent <= ?"); parameters.add(query.maxRentCent()); }
        if (query.rooms() != null) { where.append(" AND u.room_count = ?"); parameters.add(query.rooms()); }
        String select = "SELECT l.id,l.title,l.community_name,l.district,l.address,l.rent_cent,l.deposit_cent,l.tags_json,l.is_special,u.room_count,u.hall_count,u.bathroom_count,u.area_sqm,u.orientation," +
                "(SELECT m.url FROM listing_media m WHERE m.listing_id=l.id ORDER BY m.sort_no LIMIT 1) cover_url," +
                (tenantId == null ? "0" : "EXISTS(SELECT 1 FROM tenant_favorite f WHERE f.tenant_id=" + tenantId + " AND f.listing_id=l.id)") + " favorite " +
                "FROM house_listing l JOIN property_unit u ON u.id=l.unit_id";
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM house_listing l JOIN property_unit u ON u.id=l.unit_id" + where, Integer.class, parameters.toArray());
        String orderBy = switch (query.sort()) { case "rentAsc" -> "l.rent_cent ASC"; case "rentDesc" -> "l.rent_cent DESC"; case "areaDesc" -> "u.area_sqm DESC"; default -> "l.is_special DESC,l.special_sort ASC,l.published_at DESC"; };
        int page = Math.max(1, query.page()); int size = Math.min(50, Math.max(1, query.size()));
        parameters.add(size); parameters.add((page - 1) * size);
        List<ListingView> result = jdbc.query(select + where + " ORDER BY " + orderBy + " LIMIT ? OFFSET ?", listingMapper(), parameters.toArray());
        return PageResponse.of(result, count == null ? 0 : count, page, size);
    }

    @Transactional
    public ListingDetailView detail(long listingId) {
        Long tenantId = optionalTenantId();
        String sql = "SELECT l.id,l.title,l.community_name,l.district,l.address,l.rent_cent,l.deposit_cent,l.tags_json,l.facilities_json,l.description,l.is_special,u.room_count,u.hall_count,u.bathroom_count,u.area_sqm,u.orientation,(SELECT m.url FROM listing_media m WHERE m.listing_id=l.id ORDER BY m.sort_no LIMIT 1) cover_url," +
                (tenantId == null ? "0" : "EXISTS(SELECT 1 FROM tenant_favorite f WHERE f.tenant_id=" + tenantId + " AND f.listing_id=l.id)") + " favorite FROM house_listing l JOIN property_unit u ON u.id=l.unit_id WHERE l.id=? AND l.publish_status='PUBLISHED'";
        DetailRow row = jdbc.query(sql, rs -> rs.next() ? mapDetail(rs) : null, listingId);
        if (row == null) throw new BusinessException("LISTING_NOT_FOUND", "房源不存在或已下架", HttpStatus.NOT_FOUND);
        if (tenantId != null) recordHistory(tenantId, listingId);
        List<ListingDetailView.MediaView> media = jdbc.query("SELECT id,media_type,url,cover_url,sort_no FROM listing_media WHERE listing_id=? ORDER BY sort_no", (rs, n) -> new ListingDetailView.MediaView(String.valueOf(rs.getLong("id")), rs.getString("media_type"), rs.getString("url"), rs.getString("cover_url"), rs.getInt("sort_no")), listingId);
        ListingView listing = new ListingView(String.valueOf(row.id), row.title, row.community, row.district, row.address, row.rent, row.deposit, row.rooms, row.halls, row.bathrooms, row.area, row.orientation, jsonList(row.tags), row.cover, row.favorite, row.special);
        return new ListingDetailView(listing, row.description, jsonList(row.facilities), media);
    }

    @Transactional
    public void favorite(long listingId) { setFavorite(listingId, true); }
    @Transactional
    public void unfavorite(long listingId) { setFavorite(listingId, false); }
    public PageResponse<ListingView> favorites(int page, int size) { return byRelation("tenant_favorite", "created_at", page, size); }
    public PageResponse<ListingView> history(int page, int size) { return byRelation("tenant_browse_history", "viewed_at", page, size); }
    private PageResponse<ListingView> byRelation(String table, String timeColumn, int page, int size) {
        CurrentUser.requireRole(UserRole.TENANT); long tenantId = CurrentUser.require().id(); page = Math.max(1, page); size = Math.min(50, Math.max(1, size));
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE tenant_id=?", Long.class, tenantId);
        String sql = "SELECT l.id,l.title,l.community_name,l.district,l.address,l.rent_cent,l.deposit_cent,l.tags_json,l.is_special,u.room_count,u.hall_count,u.bathroom_count,u.area_sqm,u.orientation,(SELECT m.url FROM listing_media m WHERE m.listing_id=l.id ORDER BY m.sort_no LIMIT 1) cover_url,1 favorite FROM " + table + " r JOIN house_listing l ON l.id=r.listing_id JOIN property_unit u ON u.id=l.unit_id WHERE r.tenant_id=? ORDER BY r." + timeColumn + " DESC LIMIT ? OFFSET ?";
        return PageResponse.of(jdbc.query(sql, listingMapper(), tenantId, size, (page - 1) * size), total == null ? 0 : total, page, size);
    }
    private void setFavorite(long listingId, boolean enabled) {
        CurrentUser.requireRole(UserRole.TENANT); long tenantId = CurrentUser.require().id();
        if (enabled) { if (jdbc.queryForObject("SELECT COUNT(*) FROM house_listing WHERE id=? AND publish_status='PUBLISHED'", Integer.class, listingId) == 0) throw new BusinessException("LISTING_NOT_FOUND", "房源不存在", HttpStatus.NOT_FOUND); jdbc.update("INSERT IGNORE INTO tenant_favorite(tenant_id,listing_id,created_at) VALUES(?,?,?)", tenantId, listingId, LocalDateTime.now()); }
        else jdbc.update("DELETE FROM tenant_favorite WHERE tenant_id=? AND listing_id=?", tenantId, listingId);
    }
    private void recordHistory(long tenantId, long listingId) { jdbc.update("INSERT INTO tenant_browse_history(id,tenant_id,listing_id,viewed_at) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE viewed_at=VALUES(viewed_at)", idGenerator.nextId(), tenantId, listingId, LocalDateTime.now()); }
    private Long optionalTenantId() { try { return CurrentUser.require().role() == UserRole.TENANT ? CurrentUser.require().id() : null; } catch (BusinessException ignored) { return null; } }
    private RowMapper<ListingView> listingMapper() { return (rs, n) -> new ListingView(String.valueOf(rs.getLong("id")), rs.getString("title"), rs.getString("community_name"), rs.getString("district"), rs.getString("address"), rs.getInt("rent_cent"), rs.getInt("deposit_cent"), rs.getInt("room_count"), rs.getInt("hall_count"), rs.getInt("bathroom_count"), rs.getDouble("area_sqm"), rs.getString("orientation"), jsonList(rs.getString("tags_json")), rs.getString("cover_url"), rs.getBoolean("favorite"), rs.getBoolean("is_special")); };
    private DetailRow mapDetail(ResultSet rs) throws SQLException { return new DetailRow(rs.getLong("id"), rs.getString("title"), rs.getString("community_name"), rs.getString("district"), rs.getString("address"), rs.getInt("rent_cent"), rs.getInt("deposit_cent"), rs.getInt("room_count"), rs.getInt("hall_count"), rs.getInt("bathroom_count"), rs.getDouble("area_sqm"), rs.getString("orientation"), rs.getString("tags_json"), rs.getString("facilities_json"), rs.getString("description"), rs.getString("cover_url"), rs.getBoolean("favorite"), rs.getBoolean("is_special")); }
    private List<String> jsonList(String json) { try { return json == null ? List.of() : objectMapper.readValue(json, new TypeReference<>() { }); } catch (Exception ignored) { return List.of(); } }
    private record DetailRow(long id, String title, String community, String district, String address, int rent, int deposit, int rooms, int halls, int bathrooms, double area, String orientation, String tags, String facilities, String description, String cover, boolean favorite, boolean special) { }
    public record SearchQuery(String keyword, String district, Integer minRentCent, Integer maxRentCent, Integer rooms, String sort, int page, int size) { }
}
