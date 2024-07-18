package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.model.UserRoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDto {

  @Schema(example = "kyoomin@naver.com")
  @NotBlank(message = "email은 필수 입력 값입니다.")
  @Email(message = "email 형식에 맞지 않습니다.")
  private String email;

  @Schema(example = "kyoominlee@1")
  @NotBlank(message = "password은 필수 입력 값입니다.")
  @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "비밀번호 형식이 잘못 되었습니다.")
  private String password;

  @Schema(example = "kyoominlee@1")
  @NotBlank(message = "password은 필수 입력 값입니다.")
  @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "비밀번호 형식이 잘못 되었습니다.")
  private String confirmPassword;

  @Schema(example = "kyoomin")
  @NotBlank(message = "name은 필수 입력 값입니다.")
  private String name;

  @Schema(example = "TRAINER", allowableValues = {"TRAINEE", "TRAINER"})
  @NotNull(message = "role은 필수 입력 값입니다.")
  private UserRoleType role;

}
