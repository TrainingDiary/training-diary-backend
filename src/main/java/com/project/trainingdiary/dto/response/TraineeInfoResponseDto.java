package com.project.trainingdiary.dto.response;

import com.project.trainingdiary.dto.traineeRecordDto.BodyFatHistoryDto;
import com.project.trainingdiary.dto.traineeRecordDto.MuscleMassHistoryDto;
import com.project.trainingdiary.dto.traineeRecordDto.WeightHistoryDto;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.model.GenderType;
import com.project.trainingdiary.model.TargetType;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TraineeInfoResponseDto {

  private long traineeId;
  private String name;
  private int age;
  private GenderType gender;
  private double height;
  private int remainingSessions;

  private List<WeightHistoryDto> weightHistory;
  private List<BodyFatHistoryDto> bodyFatHistory;
  private List<MuscleMassHistoryDto> muscleMassHistory;
  private TargetType targetType;
  private double targetValue;
  private String targetReward;

  public static TraineeInfoResponseDto fromEntity(TraineeEntity trainee, int remainingSessions) {
    return TraineeInfoResponseDto.builder()
        .traineeId(trainee.getId())
        .name(trainee.getName())
        .age(calculateAge(trainee.getBirthDate()))
        .gender(trainee.getGender())
        .height(trainee.getHeight())
        .remainingSessions(remainingSessions)
        .weightHistory(trainee.getInBodyRecords().stream()
            .map(WeightHistoryDto::fromEntity)
            .collect(Collectors.toList()))
        .bodyFatHistory(trainee.getInBodyRecords().stream()
            .map(BodyFatHistoryDto::fromEntity)
            .collect(Collectors.toList()))
        .muscleMassHistory(trainee.getInBodyRecords().stream()
            .map(MuscleMassHistoryDto::fromEntity)
            .collect(Collectors.toList()))
        .targetType(trainee.getTargetType())
        .targetValue(trainee.getTargetValue())
        .targetReward(trainee.getTargetReward())
        .build();
  }

  private static int calculateAge(LocalDate birthDate) {
    if (birthDate != null) {
      return Period.between(birthDate, LocalDate.now()).getYears();
    } else {
      return 0;
    }
  }
}