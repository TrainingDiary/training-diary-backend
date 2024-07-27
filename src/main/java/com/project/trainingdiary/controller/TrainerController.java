package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.trainer.AddInBodyInfoRequestDto;
import com.project.trainingdiary.dto.request.trainer.EditTraineeInfoRequestDto;
import com.project.trainingdiary.dto.response.trainer.AddInBodyInfoResponseDto;
import com.project.trainingdiary.dto.response.trainer.EditTraineeInfoResponseDto;
import com.project.trainingdiary.dto.response.trainer.TraineeInfoResponseDto;
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

@Tag(name = "2 - Trainer API", description = "트레이너가 트레이니 정보를 관리하기 위한 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/trainers")
public class TrainerController {

  private final TrainerService trainerService;

  @Operation(
      summary = "트레이니 정보 조회",
      description = "트레이너 및 트레이니가 트레이니 정보를 조회합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "다른 트레이니의 정보를 볼 없습니다.", content = @Content),
      @ApiResponse(responseCode = "404", description = "계약이 없습니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER') or hasRole('TRAINEE')")
  @GetMapping("/trainees/{id}")
  public ResponseEntity<TraineeInfoResponseDto> getTraineeInfo(
      @PathVariable Long id
  ) {
    return ResponseEntity.ok(trainerService.getTraineeInfo(id));
  }

  @Operation(
      summary = "트레이니 정보 수정",
      description = "트레이너가 트레이니 정보를 수정합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PutMapping("/trainees")
  public ResponseEntity<EditTraineeInfoResponseDto> editTraineeInfo(
      @RequestBody @Valid EditTraineeInfoRequestDto dto
  ) {
    return ResponseEntity.ok(trainerService.editTraineeInfo(dto));
  }

  @Operation(
      summary = "트레이니 인바디 정보 추가",
      description = "트레이너가 트레이니의 인바디 정보를 추가합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping("/trainees")
  public ResponseEntity<AddInBodyInfoResponseDto> addInBodyRecord(
      @RequestBody @Valid AddInBodyInfoRequestDto dto
  ) {
    return ResponseEntity.ok(trainerService.addInBodyRecord(dto));
  }
}