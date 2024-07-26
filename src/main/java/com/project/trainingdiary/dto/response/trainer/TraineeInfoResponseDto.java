package com.project.trainingdiary.dto.response.trainer;

import com.project.trainingdiary.dto.request.trainer.BodyFatHistoryDto;
import com.project.trainingdiary.dto.request.trainer.MuscleMassHistoryDto;
import com.project.trainingdiary.dto.request.trainer.WeightHistoryDto;
import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.model.type.GenderType;
import com.project.trainingdiary.model.type.TargetType;
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

  private Long traineeId;
  private String name;
  private int age;
  private GenderType gender;
  private double height;
  private LocalDate birthDate;
  private int remainingSession;

  private List<WeightHistoryDto> weightHistory;
  private List<BodyFatHistoryDto> bodyFatHistory;
  private List<MuscleMassHistoryDto> muscleMassHistory;
  private TargetType targetType;
  private double targetValue;
  private String targetReward;

  public static TraineeInfoResponseDto fromEntity(TraineeEntity trainee, int remainingSession) {
    List<InBodyRecordHistoryEntity> inBodyRecords = trainee.getInBodyRecords();
    return TraineeInfoResponseDto.builder()
        .traineeId(trainee.getId())
        .name(trainee.getName())
        .age(calculateAge(trainee.getBirthDate()))
        .birthDate(trainee.getBirthDate())
        .gender(trainee.getGender())
        .height(trainee.getHeight())
        .remainingSession(remainingSession)
        .weightHistory(mapToWeightHistory(inBodyRecords))
        .bodyFatHistory(mapToBodyFatHistory(inBodyRecords))
        .muscleMassHistory(mapToMuscleMassHistory(inBodyRecords))
        .targetType(trainee.getTargetType())
        .targetValue(trainee.getTargetValue())
        .targetReward(trainee.getTargetReward())
        .build();
  }

  private static int calculateAge(LocalDate birthDate) {
    return birthDate != null ? Period.between(birthDate, LocalDate.now()).getYears() : 0;
  }

  private static List<WeightHistoryDto> mapToWeightHistory(
      List<InBodyRecordHistoryEntity> inBodyRecords) {
    return inBodyRecords.stream()
        .map(WeightHistoryDto::fromEntity)
        .collect(Collectors.toList());
  }

  private static List<BodyFatHistoryDto> mapToBodyFatHistory(
      List<InBodyRecordHistoryEntity> inBodyRecords) {
    return inBodyRecords.stream()
        .map(BodyFatHistoryDto::fromEntity)
        .collect(Collectors.toList());
  }

  private static List<MuscleMassHistoryDto> mapToMuscleMassHistory(
      List<InBodyRecordHistoryEntity> inBodyRecords) {
    return inBodyRecords.stream()
        .map(MuscleMassHistoryDto::fromEntity)
        .collect(Collectors.toList());
  }
}