package com.renthouse.appointment.controller;
import com.renthouse.common.api.*;
import com.renthouse.appointment.service.AppointmentService;
import com.renthouse.appointment.enums.AppointmentStatus;
import com.renthouse.appointment.vo.AppointmentView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/v1")
public class AppointmentController {
 private final AppointmentService service; public AppointmentController(AppointmentService service){this.service=service;}
 @PostMapping("/tenant/listings/{listingId}/appointments") public ApiResponse<AppointmentView> create(@PathVariable long listingId,@RequestBody @Valid CreateRequest r){return ApiResponse.ok(service.create(listingId,new AppointmentService.CreateCommand(r.scheduledAt(),r.contactName(),r.contactMobile(),r.note())));}
 @GetMapping("/appointments") public ApiResponse<PageResponse<AppointmentView>> list(@RequestParam(required=false) String status,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="20")int size){return ApiResponse.ok(service.list(status,page,size));}
 @PatchMapping("/appointments/{id}/status") public ApiResponse<AppointmentView> status(@PathVariable long id,@RequestBody @Valid StatusRequest r){return ApiResponse.ok(service.updateStatus(id,r.status(),r.reason()));}
 public record CreateRequest(@NotNull LocalDateTime scheduledAt,@NotBlank String contactName,@NotBlank @Pattern(regexp="^1\\d{10}$") String contactMobile,@Size(max=500)String note){}
 public record StatusRequest(@NotNull AppointmentStatus status,@Size(max=255)String reason){}
}
