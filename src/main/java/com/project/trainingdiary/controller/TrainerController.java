package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.AddInBodyInfoRequestDto;
import com.project.trainingdiary.dto.request.EditTraineeInfoRequestDto;
import com.project.trainingdiary.dto.response.AddInBodyInfoResponseDto;
import com.project.trainingdiary.dto.response.EditTraineeInfoResponseDto;
import com.project.trainingdiary.dto.response.TraineeInfoResponseDto;
import com.project.trainingdiary.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "트레이너 관리 / 대시보드", description = "트레이너가 트레이니 정보를 관리하기 위한 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/trainers")
public class TrainerController {

  private final TrainerService trainerService;

  @Operation(summary = "트레이니 정보 조회", description = "트레이너가 트레이니 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "404", description = "트레이니를 찾을 수 없습니다.", content = @Content),
      @ApiResponse(responseCode = "460", description = "트레이너와 트레이니 간의 계약이 존재하지 않습니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @GetMapping("/trainees/{id}")
  public ResponseEntity<TraineeInfoResponseDto> getTraineeInfo(
      @PathVariable Long id
  ) {
    TraineeInfoResponseDto traineeInfo = trainerService.getTraineeInfo(id);
    return ResponseEntity.ok(traineeInfo);
  }

  @Operation(summary = "트레이니 정보 수정", description = "트레이너가 트레이니 정보를 수정합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "404", description = "트레이니를 찾을 수 없습니다.", content = @Content),
      @ApiResponse(responseCode = "460", description = "트레이너와 트레이니 간의 계약이 존재하지 않습니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PutMapping("/trainees/")
  public ResponseEntity<EditTraineeInfoResponseDto> editTraineeInfo(
      @RequestBody @Valid EditTraineeInfoRequestDto dto
  ) {
    EditTraineeInfoResponseDto editTraineeInfo = trainerService.editTraineeInfo(dto);
    return ResponseEntity.ok(editTraineeInfo);
  }

  @Operation(summary = "트레이니 인바디 정보 추가", description = "트레이너가 트레이니의 인바디 정보를 추가합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "추가 성공"),
      @ApiResponse(responseCode = "404", description = "트레이니를 찾을 수 없습니다.", content = @Content),
      @ApiResponse(responseCode = "460", description = "트레이너와 트레이니 간의 계약이 존재하지 않습니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping("/trainees/")
  public ResponseEntity<AddInBodyInfoResponseDto> addInBodyRecord(
      @RequestBody @Valid AddInBodyInfoRequestDto dto
  ) {
    AddInBodyInfoResponseDto addInBodyInfo = trainerService.addInBodyRecord(dto);
    return ResponseEntity.ok(addInBodyInfo);
  }
}