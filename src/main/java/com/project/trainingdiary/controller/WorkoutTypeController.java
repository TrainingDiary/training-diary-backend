package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.WorkoutTypeCreateRequestDto;
import com.project.trainingdiary.dto.request.WorkoutTypeUpdateRequestDto;
import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.dto.response.WorkoutTypeResponseDto;
import com.project.trainingdiary.service.WorkoutTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO
//  로그인 후 운동 종류 다루기 때문에 트레이너 id 넣는 부분 수정 필요

@RestController
@RequiredArgsConstructor
@RequestMapping("api/trainers/workout-types")
public class WorkoutTypeController {

  private final WorkoutTypeService workoutTypeService;

  @PostMapping
  public CommonResponse<?> createWorkoutType(
      @Validated @RequestBody WorkoutTypeCreateRequestDto dto
  ) {
    WorkoutTypeResponseDto responseDto = workoutTypeService.createWorkoutType(dto);
    return CommonResponse.created(responseDto);
  }

  @PutMapping("/{id}")
  public CommonResponse<?> updateWorkoutType(
      @PathVariable Long id,
      @Validated @RequestBody WorkoutTypeUpdateRequestDto dto
  ) {
    WorkoutTypeResponseDto responseDto = workoutTypeService.updateWorkoutType(1L, id, dto);
    return CommonResponse.success(responseDto);
  }

  @DeleteMapping("/{id}")
  public CommonResponse<?> deleteWorkoutType(@PathVariable Long id) {
    workoutTypeService.deleteWorkoutType(1L, id);
    return CommonResponse.success("운동 종류 삭제가 완료되었습니다.");
  }

  @GetMapping
  public CommonResponse<?> getWorkoutTypes(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
    Page<WorkoutTypeResponseDto> responsePage = workoutTypeService.getWorkoutTypes(1L, pageable);
    return CommonResponse.success(responsePage);
  }

}
