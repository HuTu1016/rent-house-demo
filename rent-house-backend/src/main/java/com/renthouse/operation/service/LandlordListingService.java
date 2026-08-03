package com.renthouse.operation.service;

import com.renthouse.common.api.PageResponse;
import com.renthouse.operation.LandlordListingController.Create;
import com.renthouse.operation.LandlordListingController.Item;
import com.renthouse.operation.LandlordListingController.Media;
import com.renthouse.operation.LandlordListingController.Special;

public interface LandlordListingService {
    Item create(Create r);
    PageResponse<Item> list(int page, int size);
    void publish(long id);
    void offline(long id);
    void updateSpecial(long id, Special r);
    void addMedia(long id, Media r);
}
