package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.schedule.AcceptScheduleRequestDto;
import com.project.trainingdiary.dto.request.schedule.ApplyScheduleRequestDto;
import com.project.trainingdiary.dto.request.schedule.CancelScheduleByTraineeRequestDto;
import com.project.trainingdiary.dto.request.schedule.CancelScheduleByTrainerRequestDto;
import com.project.trainingdiary.dto.request.schedule.CloseScheduleRequestDto;
import com.project.trainingdiary.dto.request.schedule.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.request.schedule.RegisterScheduleRequestDto;
import com.project.trainingdiary.dto.request.schedule.RejectScheduleRequestDto;
import com.project.trainingdiary.dto.response.schedule.ApplyScheduleResponseDto;
import com.project.trainingdiary.dto.response.schedule.CancelScheduleByTraineeResponseDto;
import com.project.trainingdiary.dto.response.schedule.CancelScheduleByTrainerResponseDto;
import com.project.trainingdiary.dto.response.schedule.RegisterScheduleResponseDto;
import com.project.trainingdiary.dto.response.schedule.RejectScheduleResponseDto;
import com.project.trainingdiary.dto.response.schedule.ScheduleResponseDto;
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
      @ApiResponse(responseCode = "200", description = "성공")
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
      @ApiResponse(responseCode = "200", description = "성공")
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
      @ApiResponse(responseCode = "200", description = "성공")
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
      @ApiResponse(responseCode = "200", description = "성공")
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
      @ApiResponse(responseCode = "406", description = "남은 PT 횟수가 부족합니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINEE')")
  @PostMapping("/trainees/apply")
  public ResponseEntity<ApplyScheduleResponseDto> applySchedule(
      @RequestBody @Valid ApplyScheduleRequestDto dto
  ) {
    return ResponseEntity.ok(scheduleTraineeService.applySchedule(dto, LocalDateTime.now()));
  }

  @Operation(
      summary = "트레이너의 일정 수락",
      description = "트레이너가 일정을 수락함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공")
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
      @ApiResponse(responseCode = "200", description = "성공")
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
      @ApiResponse(responseCode = "406", description = "남은 PT 횟수가 부족합니다.", content = @Content)
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
      @ApiResponse(responseCode = "200", description = "성공")
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
      @ApiResponse(responseCode = "200", description = "성공")
  })
  @PreAuthorize("hasRole('TRAINEE')")
  @PostMapping("/trainees/cancel")
  public ResponseEntity<CancelScheduleByTraineeResponseDto> cancelScheduleByTrainer(
      @RequestBody @Valid CancelScheduleByTraineeRequestDto dto
  ) {
    return ResponseEntity.ok(scheduleTraineeService.cancelSchedule(dto, LocalDateTime.now()));
  }
}
