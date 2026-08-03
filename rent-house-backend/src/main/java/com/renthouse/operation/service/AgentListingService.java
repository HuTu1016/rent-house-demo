package com.renthouse.operation.service;
import com.renthouse.common.api.PageResponse;
import com.renthouse.operation.controller.AgentListingController.Create;
import com.renthouse.operation.controller.AgentListingController.Item;
import com.renthouse.operation.controller.AgentListingController.Media;
import com.renthouse.operation.controller.AgentListingController.Special;
public interface AgentListingService { Item create(Create request); PageResponse<Item> list(int page, int size); void publish(long id); void offline(long id); void updateSpecial(long id, Special request); void addMedia(long id, Media request); }
