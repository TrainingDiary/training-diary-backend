package com.project.trainingdiary.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyCodeRequestDto {

  @NotBlank(message = "email은 필수 입력 값입니다.")
  @Email(message = "email 형식에 맞지 않습니다.")
  private String email;

  @NotBlank(message = "인증 번호는 필수 입력 값입니다.")
  private String verificationCode;
}
