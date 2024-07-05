package com.project.trainingdiary.dto.request;

import com.project.trainingdiary.model.UserRoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class SignUpRequestDto {

  @NotBlank(message = "email은 필수 입력 값입니다.")
  @Email(message = "email 형식에 맞지 않습니다.")
  private String email;

  @NotBlank(message = "인증 번호는 필수 입력 값입니다.")
  @Length(min = 6, max = 6, message = "인증 번호는 6개의 숫자로 만듭니다.")
  private String verificationCode;

  @NotBlank(message = "password은 필수 입력 값입니다.")
  @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "비밀번호 형식이 잘못 되었습니다.")
  private String password;

  @NotBlank(message = "password은 필수 입력 값입니다.")
  @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "비밀번호 형식이 잘못 되었습니다.")
  private String confirmPassword;

  @NotBlank(message = "name은 필수 입력 값입니다.")
  private String name;

  @NotNull(message = "role은 필수 입력 값입니다.")
  private UserRoleType role;

}
