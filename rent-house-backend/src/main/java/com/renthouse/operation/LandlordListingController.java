package com.renthouse.operation;
import com.renthouse.auth.*;import com.renthouse.common.api.*;import com.renthouse.common.exception.BusinessException;import com.renthouse.common.id.SnowflakeIdGenerator;import jakarta.validation.*;import jakarta.validation.constraints.*;import java.time.*;import org.springframework.http.HttpStatus;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/v1/landlord/listings") public class LandlordListingController{private final com.renthouse.operation.service.LandlordListingService s;public LandlordListingController(com.renthouse.operation.service.LandlordListingService s){this.s=s;}
 @PostMapping public ApiResponse<Item>create(@RequestBody @Valid Create r){return ApiResponse.ok(s.create(r));}
 @GetMapping public ApiResponse<PageResponse<Item>>list(@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="20")int size){return ApiResponse.ok(s.list(page,size));}
 @PostMapping("/{id}/publish")public ApiResponse<Void>publish(@PathVariable long id){s.publish(id);return ApiResponse.ok();}
 @PostMapping("/{id}/offline")public ApiResponse<Void>offline(@PathVariable long id){s.offline(id);return ApiResponse.ok();}
 @PatchMapping("/{id}/special")public ApiResponse<Void>special(@PathVariable long id,@RequestBody @Valid Special r){s.updateSpecial(id,r);return ApiResponse.ok();}
 @PostMapping("/{id}/media")public ApiResponse<Void>media(@PathVariable long id,@RequestBody @Valid Media r){s.addMedia(id,r);return ApiResponse.ok();}
 public record Item(String id,String title,int rentCent,String publishStatus,boolean special,String occupancyStatus){}public record Create(@Positive long unitId,@NotBlank String title,@NotBlank String communityName,@NotBlank String district,@NotBlank String address,@Positive int rentCent,@PositiveOrZero int depositCent){}public record Special(boolean enabled,@Min(0)int sort){}public record Media(@NotBlank String type,@NotBlank String url,String coverUrl,@Min(0)int sort){}
}
