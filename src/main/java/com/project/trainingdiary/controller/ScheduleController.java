package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.OpenScheduleRequestDto;
import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.service.ScheduleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/schedules")
public class ScheduleController {

  private final ScheduleService scheduleService;

  @PostMapping("/trainers/open")
  public CommonResponse<?> openSchedule(
      @RequestBody OpenScheduleRequestDto dto
  ) {
    scheduleService.createSchedule(dto);
    return CommonResponse.success();
  }
}
