package com.renthouse.conversation;

import java.time.LocalDateTime;

public record MessageView(String id, String senderId, String messageType, String content, String appointmentId,
                          LocalDateTime createdAt, boolean mine) { }
