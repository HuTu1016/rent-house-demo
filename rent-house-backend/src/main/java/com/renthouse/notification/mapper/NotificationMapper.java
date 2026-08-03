package com.renthouse.notification.mapper;

import com.renthouse.notification.NotificationController.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface NotificationMapper {

    long countNotifications(@Param("userId") long userId);

    List<Map<String, Object>> listNotifications(@Param("userId") long userId, @Param("offset") int offset, @Param("size") int size);

    long countUnread(@Param("userId") long userId);

    void readNotification(@Param("id") long id, @Param("userId") long userId, @Param("now") LocalDateTime now);

    void readAllNotifications(@Param("userId") long userId, @Param("now") LocalDateTime now);
}
