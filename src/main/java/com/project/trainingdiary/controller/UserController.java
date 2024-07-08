package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.SendVerificationAndCheckDuplicateRequestDto;
import com.project.trainingdiary.dto.request.SignInRequestDto;
import com.project.trainingdiary.dto.request.SignOutRequestDto;
import com.project.trainingdiary.dto.request.SignUpRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;
import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.dto.response.SignInResponseDto;
import com.project.trainingdiary.model.SuccessMessage;
import com.project.trainingdiary.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/check-duplicate-and-send-verification")
  public CommonResponse<?> checkDuplicateAndSendVerification(
      @RequestBody @Valid SendVerificationAndCheckDuplicateRequestDto dto
  ) {
    userService.checkDuplicateEmailAndSendVerification(dto);
    return CommonResponse.success(SuccessMessage.SENT_VERIFICATION_SUCCESS);
  }

  @PostMapping("/check-verification-code")
  public CommonResponse<?> verifyCode(
      @RequestBody @Valid VerifyCodeRequestDto dto
  ) {
    userService.checkVerificationCode(dto);
    return CommonResponse.success(SuccessMessage.VERIFICATION_SUCCESS);
  }

  @PostMapping("/sign-up")
  public CommonResponse<?> signUp(
      @RequestBody @Valid SignUpRequestDto dto
  ) {
    userService.signUp(dto);
    return CommonResponse.created(SuccessMessage.SIGN_UP_SUCCESS);
  }

  @PostMapping("/sign-in")
  public CommonResponse<?> signIn(
      @RequestBody @Valid SignInRequestDto dto
  ) {
    SignInResponseDto response = userService.signIn(dto);
    return CommonResponse.success(response);
  }

  @PostMapping("/sign-out")
  public CommonResponse<?> signOut(
      @RequestBody @Valid SignOutRequestDto dto
  ) {
    userService.signOut(dto);
    return CommonResponse.success(SuccessMessage.SIGN_OUT_SUCCESS);
  }
}
