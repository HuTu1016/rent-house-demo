package com.renthouse.operation.mapper;

import com.renthouse.operation.LandlordListingController.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LandlordListingMapper {

    int countUnit(@Param("unitId") long unitId, @Param("landlordId") long landlordId);

    void insertListing(@Param("id") long id, @Param("unitId") long unitId, @Param("landlordId") long landlordId,
                       @Param("title") String title, @Param("communityName") String communityName,
                       @Param("district") String district, @Param("address") String address,
                       @Param("rentCent") int rentCent, @Param("depositCent") int depositCent,
                       @Param("now") LocalDateTime now);

    long countListings(@Param("landlordId") long landlordId);

    List<Item> listListings(@Param("landlordId") long landlordId, @Param("offset") int offset, @Param("size") int size);

    int publishListing(@Param("id") long id, @Param("landlordId") long landlordId, @Param("now") LocalDateTime now);

    int offlineListing(@Param("id") long id, @Param("landlordId") long landlordId, @Param("now") LocalDateTime now);

    int updateSpecial(@Param("id") long id, @Param("landlordId") long landlordId, @Param("enabled") boolean enabled, @Param("sort") int sort, @Param("now") LocalDateTime now);

    int countListing(@Param("id") long id, @Param("landlordId") long landlordId);

    void insertMedia(@Param("id") long id, @Param("listingId") long listingId, @Param("type") String type, @Param("url") String url, @Param("coverUrl") String coverUrl, @Param("sort") int sort, @Param("now") LocalDateTime now);
}
