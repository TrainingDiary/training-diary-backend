package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.AddInBodyInfoRequestDto;
import com.project.trainingdiary.dto.request.EditTraineeInfoRequestDto;
import com.project.trainingdiary.dto.response.AddInBodyInfoResponseDto;
import com.project.trainingdiary.dto.response.EditTraineeInfoResponseDto;
import com.project.trainingdiary.dto.response.TraineeInfoResponseDto;
import com.project.trainingdiary.service.TrainerService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("api/trainers")
public class TrainerController {

  private final TrainerService trainerService;

  @PreAuthorize("hasRole('TRAINER')")
  @GetMapping("/trainees/{id}")
  public ResponseEntity<TraineeInfoResponseDto> getTraineeInfo(
      @PathVariable Long id
  ) {

    TraineeInfoResponseDto traineeInfo = trainerService.getTraineeInfo(id);
    return ResponseEntity.ok(traineeInfo);
  }

  @PreAuthorize("hasRole('TRAINER')")
  @PutMapping("/trainees/")
  public ResponseEntity<EditTraineeInfoResponseDto> editTraineeInfo(
      @RequestBody @Valid EditTraineeInfoRequestDto dto
  ) {
    EditTraineeInfoResponseDto editTraineeInfo = trainerService.editTraineeInfo(dto);
    return ResponseEntity.ok(editTraineeInfo);
  }

  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping("/trainees/")
  public ResponseEntity<AddInBodyInfoResponseDto> addInBodyRecord(
      @RequestBody @Valid AddInBodyInfoRequestDto dto
  ) {

    AddInBodyInfoResponseDto addInBodyInfo = trainerService.addInBodyRecord(dto);
    return ResponseEntity.ok(addInBodyInfo);
  }
}
