package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.model.ScheduleDateTimes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterScheduleRequestDto {

  @NotNull(message = "traineeId를 입력해주세요")
  @Schema(example = "1")
  private Long traineeId;

  @NotNull(message = "dateTimes를 입력해주세요")
  private List<ScheduleDateTimes> dateTimes;
}
