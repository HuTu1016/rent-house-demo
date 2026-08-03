package com.renthouse.notification.service.impl;

import com.renthouse.auth.CurrentUser;
import com.renthouse.common.api.PageResponse;
import com.renthouse.notification.NotificationController.Item;
import com.renthouse.notification.mapper.NotificationMapper;
import com.renthouse.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper mapper;

    public NotificationServiceImpl(NotificationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PageResponse<Item> list(int page, int size) {
        long u = CurrentUser.require().id();
        page = Math.max(1, page);
        size = Math.min(50, Math.max(1, size));
        int offset = (page - 1) * size;

        long t = mapper.countNotifications(u);
        List<Map<String, Object>> rows = mapper.listNotifications(u, offset, size);
        
        List<Item> items = rows.stream().map(r -> new Item(
                String.valueOf(r.get("id")),
                (String) r.get("notification_type"),
                (String) r.get("title"),
                (String) r.get("content"),
                r.get("read_at") == null,
                r.get("created_at") instanceof java.sql.Timestamp ? ((java.sql.Timestamp)r.get("created_at")).toLocalDateTime() : (LocalDateTime) r.get("created_at")
        )).collect(Collectors.toList());
        
        return PageResponse.of(items, t, page, size);
    }

    @Override
    public long unreadCount() {
        return mapper.countUnread(CurrentUser.require().id());
    }

    @Override
    @Transactional
    public void read(long id) {
        mapper.readNotification(id, CurrentUser.require().id(), LocalDateTime.now());
    }

    @Override
    @Transactional
    public void readAll() {
        mapper.readAllNotifications(CurrentUser.require().id(), LocalDateTime.now());
    }
}
