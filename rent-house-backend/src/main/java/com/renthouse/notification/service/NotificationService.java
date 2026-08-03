package com.renthouse.notification.service;

import com.renthouse.common.api.PageResponse;
import com.renthouse.notification.NotificationController.Item;

public interface NotificationService {
    PageResponse<Item> list(int page, int size);
    long unreadCount();
    void read(long id);
    void readAll();
}
