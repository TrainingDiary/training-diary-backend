package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.AcceptScheduleRequestDto;
import com.project.trainingdiary.dto.request.ApplyScheduleRequestDto;
import com.project.trainingdiary.dto.request.CancelScheduleByTraineeRequestDto;
import com.project.trainingdiary.dto.request.CancelScheduleByTrainerRequestDto;
import com.project.trainingdiary.dto.request.CloseScheduleRequestDto;
import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.request.RegisterScheduleRequestDto;
import com.project.trainingdiary.dto.request.RejectScheduleRequestDto;
import com.project.trainingdiary.dto.response.ApplyScheduleResponseDto;
import com.project.trainingdiary.dto.response.CancelScheduleByTraineeResponseDto;
import com.project.trainingdiary.dto.response.CancelScheduleByTrainerResponseDto;
import com.project.trainingdiary.dto.response.RegisterScheduleResponseDto;
import com.project.trainingdiary.dto.response.RejectScheduleResponseDto;
import com.project.trainingdiary.dto.response.ScheduleResponseDto;
import com.project.trainingdiary.service.ScheduleOpenCloseService;
import com.project.trainingdiary.service.ScheduleTraineeService;
import com.project.trainingdiary.service.ScheduleTrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "5 - Schedule API", description = "일정 예약을 위한 API")
@RestController
@AllArgsConstructor
@RequestMapping("api/schedules")
public class ScheduleController {

  private final ScheduleTrainerService scheduleTrainerService;
  private final ScheduleTraineeService scheduleTraineeService;
  private final ScheduleOpenCloseService scheduleOpenCloseService;

  @Operation(
      summary = "트레이너의 일정 열기",
      description = "트레이너가 예약 가능한 일정을 열어놓음"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "409", description = "같은 시간에 이미 일정이 존재합니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping("/trainers/open")
  public ResponseEntity<Void> openSchedule(
      @RequestBody @Valid OpenScheduleRequestDto dto
  ) {
    scheduleOpenCloseService.createSchedule(dto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "트레이너의 일정 조회",
      description = "트레이너가 기간 내의 일정을 조회함. (조회 간격은 최대 180일)"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "413", description = "일정 간격이 너무 깁니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @GetMapping("/trainers")
  public ResponseEntity<List<ScheduleResponseDto>> getScheduleListByTrainer(
      @RequestParam LocalDate startDate,
      @RequestParam LocalDate endDate
  ) {
    return ResponseEntity.ok(scheduleTrainerService.getScheduleList(startDate, endDate));
  }

  @Operation(
      summary = "트레이니의 일정 조회",
      description = "트레이니가 기간 내의 일정을 조회함. (조회 간격은 최대 180일)"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "413", description = "일정 간격이 너무 깁니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINEE')")
  @GetMapping("/trainees")
  public ResponseEntity<List<ScheduleResponseDto>> getScheduleListByTrainee(
      @RequestParam LocalDate startDate,
      @RequestParam LocalDate endDate
  ) {
    return ResponseEntity.ok(scheduleTraineeService.getScheduleList(startDate, endDate));
  }

  @Operation(
      summary = "트레이너의 일정 닫기",
      description = "트레이너가 예약 가능한 일정을 닫음"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "일정이 없습니다.", content = @Content),
      @ApiResponse(responseCode = "409", description = "일정이 OPEN 상태가 아닙니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping("/trainers/close")
  public ResponseEntity<Void> closeSchedules(
      @RequestBody @Valid CloseScheduleRequestDto dto
  ) {
    scheduleOpenCloseService.closeSchedules(dto.scheduleIds);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "트레이니의 일정 신청",
      description = "트레이니가 일정을 신청함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "일정이 없습니다.", content = @Content),
      @ApiResponse(responseCode = "409", description = "일정이 OPEN 상태가 아닙니다.", content = @Content),
      @ApiResponse(responseCode = "412", description = "과거의 일정은 예약할 수 없습니다.", content = @Content),
      @ApiResponse(responseCode = "417", description = "1시간 내 시작하는 일정은 예약할 수 없습니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINEE')")
  @PostMapping("/trainees/apply")
  public ResponseEntity<ApplyScheduleResponseDto> applySchedule(
      @RequestBody @Valid ApplyScheduleRequestDto dto
  ) {
    ApplyScheduleResponseDto response = scheduleTraineeService.applySchedule(dto,
        LocalDateTime.now());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "트레이너의 일정 수락",
      description = "트레이너가 일정을 수락함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "일정이 없습니다.", content = @Content),
      @ApiResponse(responseCode = "409", description = "일정의 상태가 RESERVE_APPLIED가 아닙니다.", content = @Content),
      @ApiResponse(responseCode = "417", description = "전체 세션 갯수를 다 사용했습니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping("/trainers/accept")
  public ResponseEntity<Void> acceptSchedule(
      @RequestBody @Valid AcceptScheduleRequestDto dto
  ) {
    scheduleTrainerService.acceptSchedule(dto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "트레이너의 일정 거절",
      description = "트레이너가 일정을 거절함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "일정이 없습니다.", content = @Content),
      @ApiResponse(responseCode = "409", description = "일정의 상태가 RESERVE_APPLIED가 아닙니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping("/trainers/reject")
  public ResponseEntity<RejectScheduleResponseDto> rejectSchedule(
      @RequestBody @Valid RejectScheduleRequestDto dto
  ) {
    return ResponseEntity.ok(scheduleTrainerService.rejectSchedule(dto));
  }

  @Operation(
      summary = "트레이너의 일정 등록",
      description = "트레이너가 일정을 직접 등록함. 트레이니가 별도로 신청하는 과정을 거치지 않음"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "406", description = "PT 횟수가 부족합니다.", content = @Content),
      @ApiResponse(responseCode = "409", description = "일정이 OPEN 상태가 아닙니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping("/trainers/register")
  public ResponseEntity<RegisterScheduleResponseDto> rejectSchedule(
      @RequestBody @Valid RegisterScheduleRequestDto dto
  ) {
    return ResponseEntity.ok(scheduleOpenCloseService.registerSchedule(dto));
  }

  @Operation(
      summary = "트레이너의 일정 취소",
      description = "트레이너가 일정을 취소함. 일정에 연결된 트레이니가 없어지고 OPEN 상태로 변경됨"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "일정이 없습니다.", content = @Content),
      @ApiResponse(responseCode = "409", description = "일정의 상태가 RESERVED나 RESERVE_APPLIED가 아닙니다.", content = @Content),
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping("/trainers/cancel")
  public ResponseEntity<CancelScheduleByTrainerResponseDto> cancelScheduleByTrainer(
      @RequestBody @Valid CancelScheduleByTrainerRequestDto dto
  ) {
    return ResponseEntity.ok(scheduleTrainerService.cancelSchedule(dto));
  }

  @Operation(
      summary = "트레이니의 일정 취소",
      description = "트레이니가 일정을 취소함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "일정이 없습니다.", content = @Content),
      @ApiResponse(responseCode = "409", description = "일정의 상태가 RESERVED나 RESERVE_APPLIED가 아닙니다.", content = @Content),
      @ApiResponse(responseCode = "417", description = "일정이 하루 안에 시작되므로 취소할 수 없습니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINEE')")
  @PostMapping("/trainees/cancel")
  public ResponseEntity<CancelScheduleByTraineeResponseDto> cancelScheduleByTrainer(
      @RequestBody @Valid CancelScheduleByTraineeRequestDto dto
  ) {
    return ResponseEntity.ok(scheduleTraineeService.cancelSchedule(
        dto, LocalDateTime.now()
    ));
  }
}
