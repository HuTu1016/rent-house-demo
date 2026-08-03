package com.renthouse.listing.controller;

import com.renthouse.common.api.ApiResponse;
import com.renthouse.common.api.PageResponse;
import com.renthouse.listing.service.ListingService;
import com.renthouse.listing.vo.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class ListingController {
    private final ListingService listingService;
    public ListingController(ListingService listingService) { this.listingService = listingService; }
    @GetMapping("/listings") public ApiResponse<PageResponse<ListingView>> search(@RequestParam(required = false) String keyword, @RequestParam(required = false) String district, @RequestParam(required = false) Integer minRentCent, @RequestParam(required = false) Integer maxRentCent, @RequestParam(required = false) Integer rooms, @RequestParam(defaultValue = "recommended") String sort, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) { return ApiResponse.ok(listingService.search(new ListingService.SearchQuery(keyword, district, minRentCent, maxRentCent, rooms, sort, page, size))); }
    @GetMapping("/listings/{listingId}") public ApiResponse<ListingDetailView> detail(@PathVariable long listingId) { return ApiResponse.ok(listingService.detail(listingId)); }
    @PutMapping("/tenant/favorites/{listingId}") public ApiResponse<Void> favorite(@PathVariable long listingId) { listingService.favorite(listingId); return ApiResponse.ok(); }
    @DeleteMapping("/tenant/favorites/{listingId}") public ApiResponse<Void> unfavorite(@PathVariable long listingId) { listingService.unfavorite(listingId); return ApiResponse.ok(); }
    @GetMapping("/tenant/favorites") public ApiResponse<PageResponse<ListingView>> favorites(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) { return ApiResponse.ok(listingService.favorites(page, size)); }
    @GetMapping("/tenant/history") public ApiResponse<PageResponse<ListingView>> history(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) { return ApiResponse.ok(listingService.history(page, size)); }
}
