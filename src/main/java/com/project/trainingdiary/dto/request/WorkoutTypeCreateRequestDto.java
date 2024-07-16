package com.project.trainingdiary.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutTypeCreateRequestDto {

  @NotBlank(message = "운동 종류 이름은 필수 입력사항입니다.")
  private String name;

  @NotBlank(message = "운동 종류의 타겟 부위는 필수 입력사항입니다.")
  private String targetMuscle;

  @NotBlank(message = "운동 종류에 대한 설명을 작성해주세요.")
  private String remarks;

  private boolean weightInputRequired;
  private boolean repInputRequired;
  private boolean setInputRequired;
  private boolean timeInputRequired;
  private boolean speedInputRequired;

}

