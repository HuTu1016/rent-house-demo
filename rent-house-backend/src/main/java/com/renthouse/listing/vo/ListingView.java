package com.renthouse.listing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 房源简要信息视图对象
 */
@Schema(description = "房源列表项视图数据")
public record ListingView(
        @Schema(description = "房源 ID", example = "1001")
        String id,

        @Schema(description = "房源标题", example = "精装采光三居室")
        String title,

        @Schema(description = "小区名称", example = "幸福花园")
        String communityName,

        @Schema(description = "行政区划", example = "朝阳区")
        String district,

        @Schema(description = "详细地址", example = "建国路88号")
        String address,

        @Schema(description = "月租金（分）", example = "350000")
        int rentCent,

        @Schema(description = "押金（分）", example = "350000")
        int depositCent,

        @Schema(description = "室数", example = "3")
        int roomCount,

        @Schema(description = "厅数", example = "1")
        int hallCount,

        @Schema(description = "卫数", example = "1")
        int bathroomCount,

        @Schema(description = "建筑面积（平方米）", example = "89.5")
        double areaSqm,

        @Schema(description = "房屋朝向", example = "朝南")
        String orientation,

        @Schema(description = "标签列表", example = "[\"近地铁\", \"随时看房\"]")
        List<String> tags,

        @Schema(description = "封面图 URL", example = "https://example.com/cover.jpg")
        String coverUrl,

        @Schema(description = "当前租客是否已收藏", example = "false")
        boolean favorite,

        @Schema(description = "是否精选推荐", example = "true")
        boolean special
) { }
