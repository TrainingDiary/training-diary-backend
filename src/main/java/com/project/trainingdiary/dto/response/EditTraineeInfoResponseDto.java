package com.project.trainingdiary.dto.response;

import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.model.GenderType;
import com.project.trainingdiary.model.TargetType;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EditTraineeInfoResponseDto {

  private Long traineeId;
  private LocalDate birthDate;
  private GenderType gender;
  private double height;
  private int remainingSessions;
  private TargetType targetType;
  private double targetValue;
  private String targetReward;

  public static EditTraineeInfoResponseDto fromEntity(TraineeEntity trainee) {
    return EditTraineeInfoResponseDto.builder()
        .traineeId(trainee.getId())
        .birthDate(trainee.getBirthDate())
        .gender(trainee.getGender())
        .height(trainee.getHeight())
        .targetType(trainee.getTargetType())
        .targetValue(trainee.getTargetValue())
        .targetReward(trainee.getTargetReward())
        .build();
  }
}