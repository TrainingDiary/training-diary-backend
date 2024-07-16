package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.AcceptScheduleRequestDto;
import com.project.trainingdiary.dto.request.ApplyScheduleRequestDto;
import com.project.trainingdiary.dto.request.CloseScheduleRequestDto;
import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.request.RegisterScheduleRequestDto;
import com.project.trainingdiary.dto.request.RejectScheduleRequestDto;
import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.dto.response.RegisterScheduleResponseDto;
import com.project.trainingdiary.dto.response.RejectScheduleResponseDto;
import com.project.trainingdiary.model.SuccessMessage;
import com.project.trainingdiary.service.ScheduleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/schedules")
public class ScheduleController {

  private final ScheduleService scheduleService;

  @PostMapping("/trainers/open")
  public CommonResponse<?> openSchedule(
      @RequestBody @Valid OpenScheduleRequestDto dto
  ) {
    scheduleService.createSchedule(dto);
    return CommonResponse.success(SuccessMessage.SCHEDULE_OPEN_SUCCESS);
  }

  @GetMapping
  public CommonResponse<?> getScheduleList(
      @RequestParam LocalDate startDate,
      @RequestParam LocalDate endDate
  ) {
    return CommonResponse.success(scheduleService.getScheduleList(startDate, endDate));
  }

  @PostMapping("/trainers/close")
  public CommonResponse<?> closeSchedules(
      @RequestBody @Valid CloseScheduleRequestDto dto
  ) {
    scheduleService.closeSchedules(dto.scheduleIds);
    return CommonResponse.success();
  }

  @PostMapping("/trainees/apply")
  public CommonResponse<?> applySchedule(
      @RequestBody @Valid ApplyScheduleRequestDto dto
  ) {
    scheduleService.applySchedule(dto, LocalDateTime.now());
    return CommonResponse.success();
  }

  @PostMapping("/trainers/accept")
  public CommonResponse<?> acceptSchedule(
      @RequestBody @Valid AcceptScheduleRequestDto dto
  ) {
    scheduleService.acceptSchedule(dto);
    return CommonResponse.success();
  }

  @PostMapping("/trainers/reject")
  public ResponseEntity<RejectScheduleResponseDto> rejectSchedule(
      @RequestBody @Valid RejectScheduleRequestDto dto
  ) {
    RejectScheduleResponseDto response = scheduleService.rejectSchedule(dto);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/trainers/register")
  public ResponseEntity<RegisterScheduleResponseDto> rejectSchedule(
      @RequestBody @Valid RegisterScheduleRequestDto dto
  ) {
    RegisterScheduleResponseDto response = scheduleService.registerSchedule(dto);
    return ResponseEntity.ok(response);
  }
}
