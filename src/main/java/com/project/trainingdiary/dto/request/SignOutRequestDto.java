package com.project.trainingdiary.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignOutRequestDto {
    @NotBlank(message = "토큰은 비어 있으면 안됩니다.")
    private String token;
}
