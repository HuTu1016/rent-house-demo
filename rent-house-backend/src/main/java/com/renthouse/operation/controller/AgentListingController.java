package com.renthouse.operation.controller;

import com.renthouse.common.api.ApiResponse;
import com.renthouse.common.api.PageResponse;
import com.renthouse.operation.service.AgentListingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/agent/listings")
public class AgentListingController {
    private final AgentListingService service;
    public AgentListingController(AgentListingService service) { this.service = service; }
    @PostMapping public ApiResponse<Item> create(@RequestBody @Valid Create request) { return ApiResponse.ok(service.create(request)); }
    @GetMapping public ApiResponse<PageResponse<Item>> list(@RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size) { return ApiResponse.ok(service.list(page, size)); }
    @PostMapping("/{id}/publish") public ApiResponse<Void> publish(@PathVariable long id) { service.publish(id); return ApiResponse.ok(); }
    @PostMapping("/{id}/offline") public ApiResponse<Void> offline(@PathVariable long id) { service.offline(id); return ApiResponse.ok(); }
    @PatchMapping("/{id}/special") public ApiResponse<Void> special(@PathVariable long id, @RequestBody @Valid Special request) { service.updateSpecial(id, request); return ApiResponse.ok(); }
    @PostMapping("/{id}/media") public ApiResponse<Void> media(@PathVariable long id, @RequestBody @Valid Media request) { service.addMedia(id, request); return ApiResponse.ok(); }
    public record Item(String id, String title, int rentCent, String publishStatus, boolean special, String occupancyStatus) { }
    public record Create(@Positive long unitId, @NotBlank String title, @NotBlank String communityName, @NotBlank String district, @NotBlank String address, @Positive int rentCent, @PositiveOrZero int depositCent) { }
    public record Special(boolean enabled, @Min(0) int sort) { }
    public record Media(@NotBlank String type, @NotBlank String url, String coverUrl, @Min(0) int sort) { }
}
