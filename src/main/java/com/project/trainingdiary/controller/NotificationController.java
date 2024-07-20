package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.RegisterFcmTokenRequestDto;
import com.project.trainingdiary.dto.response.NotificationResponseDto;
import com.project.trainingdiary.service.FcmTokenService;
import com.project.trainingdiary.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "9 - Notification API", description = "알림을 위한 API")
@RestController
@AllArgsConstructor
@RequestMapping("api/notifications")
public class NotificationController {

  private final NotificationService notificationService;
  private final FcmTokenService fcmTokenService;

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

  @Operation(
      summary = "FCM 토큰 등록",
      description = "트레이너나 트레이니가 발급받은 FCM 토큰을 우리 서버에 등록함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공")
  })
  @PutMapping("/fcm-token")
  public ResponseEntity<Void> registerFcmToken(
      @RequestBody @Valid RegisterFcmTokenRequestDto dto
  ) {
    fcmTokenService.registerFcmToken(dto);
    return ResponseEntity.ok().build();
  }
}
