package com.renthouse.listing;

import java.util.List;

public record ListingView(String id, String title, String communityName, String district, String address, int rentCent,
                          int depositCent, int roomCount, int hallCount, int bathroomCount, double areaSqm,
                          String orientation, List<String> tags, String coverUrl, boolean favorite, boolean special) { }
