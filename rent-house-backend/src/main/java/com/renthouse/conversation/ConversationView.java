package com.renthouse.conversation;

import java.time.LocalDateTime;

public record ConversationView(String id, String listingId, String listingTitle, String peerId, String peerName,
                               String lastMessagePreview, LocalDateTime lastMessageAt, long unreadCount) { }
