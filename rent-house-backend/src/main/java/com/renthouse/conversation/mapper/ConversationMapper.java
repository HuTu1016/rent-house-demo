package com.renthouse.conversation.mapper;

import com.renthouse.conversation.ConversationView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ConversationMapper {

    Long getLandlordIdByListing(@Param("listingId") long listingId);

    Long getConversationIdByListingAndUsers(@Param("listingId") long listingId, @Param("tenantId") long tenantId, @Param("landlordId") long landlordId);

    void insertConversation(@Param("id") long id, @Param("listingId") long listingId, @Param("tenantId") long tenantId, @Param("landlordId") long landlordId, @Param("now") LocalDateTime now);

    ConversationView findConversationView(@Param("conversationId") long conversationId, @Param("userId") long userId);

    long countConversations(@Param("ownerColumn") String ownerColumn, @Param("userId") long userId);

    List<ConversationView> listConversations(@Param("ownerColumn") String ownerColumn, @Param("userId") long userId, @Param("offset") int offset, @Param("size") int size);

    void updateMessagesRead(@Param("conversationId") long conversationId, @Param("userId") long userId, @Param("now") LocalDateTime now);

    long countMessages(@Param("conversationId") long conversationId);

    List<Map<String, Object>> listMessages(@Param("conversationId") long conversationId, @Param("offset") int offset, @Param("size") int size);

    void insertMessage(@Param("id") long id, @Param("conversationId") long conversationId, @Param("senderId") long senderId, @Param("messageType") String messageType, @Param("content") String content, @Param("now") LocalDateTime now);

    void updateConversationPreview(@Param("conversationId") long conversationId, @Param("preview") String preview, @Param("now") LocalDateTime now);

    int countParticipant(@Param("conversationId") long conversationId, @Param("userId") long userId);
}
