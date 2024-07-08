package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.model.SuccessMessage;
import com.project.trainingdiary.service.ScheduleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
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
}
