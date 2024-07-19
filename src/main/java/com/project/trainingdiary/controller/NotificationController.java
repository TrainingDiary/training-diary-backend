package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.response.NotificationResponseDto;
import com.project.trainingdiary.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "9 - Notification API", description = "알림을 위한 API")
@RestController
@AllArgsConstructor
@RequestMapping("api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(
      summary = "알림 목록 조회",
      description = "자신의 알림 목록을 조회함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공")
  })
  @GetMapping
  public ResponseEntity<Page<NotificationResponseDto>> getNotificationList(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(notificationService.getNotificationList(pageable));
  }
}
