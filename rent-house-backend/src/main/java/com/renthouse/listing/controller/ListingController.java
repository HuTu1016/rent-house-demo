package com.renthouse.listing.controller;

import com.renthouse.common.api.ApiResponse;
import com.renthouse.common.api.PageResponse;
import com.renthouse.listing.service.ListingService;
import com.renthouse.listing.vo.ListingDetailView;
import com.renthouse.listing.vo.ListingView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 房源浏览与搜索控制器
 * <p>
 * 提供面向租客的公开房源检索、房源详情查看，以及登录租客对房源的收藏与浏览历史管理。
 */
@Tag(name = "01. 房源大厅与检索", description = "提供公开房源检索、房源详情查看、租客收藏及浏览历史管理接口")
@RestController
@RequestMapping("/v1")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    /**
     * 条件分页搜索房源
     *
     * @param keyword 搜索关键字（小区或标题）
     * @param district 行政区划
     * @param minRentCent 最低月租金（分）
     * @param maxRentCent 最高月租金（分）
     * @param rooms 居室数量
     * @param sort 排序类型 (recommended / rent_asc / rent_desc)
     * @param page 当前页码
     * @param size 每页数量
     * @return 分页房源列表
     */
    @Operation(summary = "多条件搜索房源列表", description = "支持关键字、区域、租金范围、户型及多种排序规则分页查询房源列表")
    @GetMapping("/listings")
    public ApiResponse<PageResponse<ListingView>> search(
            @Parameter(description = "搜索关键字（标题或小区名称）") @RequestParam(required = false) String keyword,
            @Parameter(description = "行政区划（如：朝阳区）") @RequestParam(required = false) String district,
            @Parameter(description = "最低租金（单位：分）") @RequestParam(required = false) Integer minRentCent,
            @Parameter(description = "最高租金（单位：分）") @RequestParam(required = false) Integer maxRentCent,
            @Parameter(description = "房间数量") @RequestParam(required = false) Integer rooms,
            @Parameter(description = "排序规则（recommended: 推荐，rent_asc: 租金从低到高，rent_desc: 租金从高到低）") @RequestParam(defaultValue = "recommended") String sort,
            @Parameter(description = "页码（从 1 开始）") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页展示记录条数") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(listingService.search(
                new ListingService.SearchQuery(keyword, district, minRentCent, maxRentCent, rooms, sort, page, size)));
    }

    /**
     * 获取指定房源详情
     *
     * @param listingId 房源 ID
     * @return 房源详细信息（包含图片及配图设施）
     */
    @Operation(summary = "查询房源详细信息", description = "根据房源 ID 获取详细基本信息、配套设施、媒体图片/视频以及收藏状态")
    @GetMapping("/listings/{listingId}")
    public ApiResponse<ListingDetailView> detail(
            @Parameter(description = "房源 ID", required = true) @PathVariable long listingId) {
        return ApiResponse.ok(listingService.detail(listingId));
    }

    /**
     * 收藏指定房源
     *
     * @param listingId 房源 ID
     * @return 成功响应
     */
    @Operation(summary = "收藏房源", description = "当前登录租客收藏指定的房源，已收藏重试保持幂等")
    @PutMapping("/tenant/favorites/{listingId}")
    public ApiResponse<Void> favorite(
            @Parameter(description = "房源 ID", required = true) @PathVariable long listingId) {
        listingService.favorite(listingId);
        return ApiResponse.ok();
    }

    /**
     * 取消收藏指定房源
     *
     * @param listingId 房源 ID
     * @return 成功响应
     */
    @Operation(summary = "取消收藏房源", description = "当前登录租客取消已收藏的房源")
    @DeleteMapping("/tenant/favorites/{listingId}")
    public ApiResponse<Void> unfavorite(
            @Parameter(description = "房源 ID", required = true) @PathVariable long listingId) {
        listingService.unfavorite(listingId);
        return ApiResponse.ok();
    }

    /**
     * 分页查询登录租客的收藏列表
     *
     * @param page 页码
     * @param size 每页条数
     * @return 收藏房源列表
     */
    @Operation(summary = "查询我的收藏房源", description = "分页获取当前登录租客已收藏的房源列表")
    @GetMapping("/tenant/favorites")
    public ApiResponse<PageResponse<ListingView>> favorites(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(listingService.favorites(page, size));
    }

    /**
     * 分页查询登录租客的浏览历史
     *
     * @param page 页码
     * @param size 每页条数
     * @return 历史浏览房源列表
     */
    @Operation(summary = "查询我的浏览历史", description = "分页获取当前登录租客最近浏览过的房源历史记录")
    @GetMapping("/tenant/history")
    public ApiResponse<PageResponse<ListingView>> history(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(listingService.history(page, size));
    }
}
