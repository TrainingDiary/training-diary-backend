package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.EmailDuplicateCheckRequestDto;
import com.project.trainingdiary.dto.request.SendVerificationEmailRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;
import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.model.SuccessMessage;
import com.project.trainingdiary.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/check-duplicate-email")
  public CommonResponse<?> checkDuplicateEmail(
      @RequestBody @Valid EmailDuplicateCheckRequestDto dto
  ) {
    userService.checkDuplicateEmail(dto);
    return CommonResponse.success(SuccessMessage.NO_DUPLICATE_EMAIL);
  }

  @PostMapping("/send-verification-email")
  public ResponseEntity<?> sendVerificationEmail(
      @RequestBody @Valid SendVerificationEmailRequestDto dto
  ) {
    userService.sendVerificationEmail(dto);
    return CommonResponse.success(SuccessMessage.SENT_VERIFICATION_SUCCESS);
  }

  @PostMapping("/check-verification-code")
  public CommonResponse<?> verifyCode(
      @RequestBody @Valid VerifyCodeRequestDto dto
  ) {
    userService.checkVerificationCode(dto);
    return CommonResponse.success(SuccessMessage.VERIFICATION_SUCCESS);
  }
}
