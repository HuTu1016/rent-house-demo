package com.renthouse.listing.vo;

import java.util.List;

public record ListingDetailView(ListingView listing, String description, List<String> facilities, List<MediaView> media) {
    public record MediaView(String id, String type, String url, String coverUrl, int sortNo) { }
}
