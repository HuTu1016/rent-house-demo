package com.renthouse.listing.mapper;

import com.renthouse.listing.vo.ListingDetailView;
import com.renthouse.listing.service.ListingService.SearchQuery;
import com.renthouse.listing.vo.ListingView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ListingMapper {

    long countListings(@Param("query") SearchQuery query);

    List<ListingView> searchListings(@Param("query") SearchQuery query, @Param("tenantId") Long tenantId, @Param("offset") int offset, @Param("size") int size, @Param("orderBy") String orderBy);

    Map<String, Object> getListingDetail(@Param("listingId") long listingId, @Param("tenantId") Long tenantId);

    List<ListingDetailView.MediaView> getListingMedia(@Param("listingId") long listingId);

    int countPublishedListing(@Param("listingId") long listingId);

    void insertFavorite(@Param("tenantId") long tenantId, @Param("listingId") long listingId, @Param("createdAt") LocalDateTime createdAt);

    void deleteFavorite(@Param("tenantId") long tenantId, @Param("listingId") long listingId);

    long countHistoryOrFavorite(@Param("table") String table, @Param("tenantId") long tenantId);

    List<ListingView> listHistoryOrFavorite(@Param("table") String table, @Param("timeColumn") String timeColumn, @Param("tenantId") long tenantId, @Param("offset") int offset, @Param("size") int size);

    void upsertHistory(@Param("id") long id, @Param("tenantId") long tenantId, @Param("listingId") long listingId, @Param("viewedAt") LocalDateTime viewedAt);
}
