package com.project.trainingdiary.dto.request.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterFcmTokenRequestDto {

  @NotNull
  @Schema(example = "tokenthatyoureceivedfromfirebasecloudmessagingserver")
  private String token;
}
