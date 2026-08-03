package com.renthouse.conversation.controller;

import com.renthouse.common.api.ApiResponse;
import com.renthouse.common.api.PageResponse;
import com.renthouse.conversation.service.ConversationService;
import com.renthouse.conversation.vo.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class ConversationController {
    private final ConversationService service;
    public ConversationController(ConversationService service) { this.service=service; }
    @PostMapping("/tenant/listings/{listingId}/conversation") public ApiResponse<ConversationView> create(@PathVariable long listingId) { return ApiResponse.ok(service.getOrCreateForTenant(listingId)); }
    @GetMapping("/conversations") public ApiResponse<PageResponse<ConversationView>> list(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="20") int size){ return ApiResponse.ok(service.list(page,size)); }
    @GetMapping("/conversations/{conversationId}/messages") public ApiResponse<PageResponse<MessageView>> messages(@PathVariable long conversationId,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="50") int size){ return ApiResponse.ok(service.messages(conversationId,page,size)); }
    @PostMapping("/conversations/{conversationId}/messages") public ApiResponse<MessageView> send(@PathVariable long conversationId,@RequestBody @Valid SendMessageRequest request){ return ApiResponse.ok(service.send(conversationId,request.content())); }
    public record SendMessageRequest(@NotBlank String content) { }
}
