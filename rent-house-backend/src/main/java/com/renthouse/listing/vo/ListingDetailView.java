package com.renthouse.listing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 房源详细信息视图对象
 */
@Schema(description = "房源详情视图数据")
public record ListingDetailView(
        @Schema(description = "房源基础概览")
        ListingView listing,

        @Schema(description = "房源详细描述信息", example = "采光极佳，紧邻地铁站...")
        String description,

        @Schema(description = "配套设施列表", example = "[\"洗衣机\", \"冰箱\", \"空调\"]")
        List<String> facilities,

        @Schema(description = "媒体图片及视频列表")
        List<MediaView> media
) {
    /**
     * 房源媒体素材视图对象
     */
    @Schema(description = "房源媒体素材")
    public record MediaView(
            @Schema(description = "媒体素材 ID", example = "201")
            String id,

            @Schema(description = "素材类型 (IMAGE/VIDEO)", example = "IMAGE")
            String type,

            @Schema(description = "资源 URL", example = "https://example.com/photo.jpg")
            String url,

            @Schema(description = "封面图 URL（针对视频）", example = "https://example.com/video_cover.jpg")
            String coverUrl,

            @Schema(description = "排序序号", example = "1")
            int sortNo
    ) { }
}
