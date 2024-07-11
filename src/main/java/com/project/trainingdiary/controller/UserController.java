package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.SendVerificationAndCheckDuplicateRequestDto;
import com.project.trainingdiary.dto.request.SignInRequestDto;
import com.project.trainingdiary.dto.request.SignUpRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;
import com.project.trainingdiary.dto.response.CustomResponse;
import com.project.trainingdiary.dto.response.MemberInfoResponseDto;
import com.project.trainingdiary.dto.response.SignInResponseDto;
import com.project.trainingdiary.model.SuccessMessage;
import com.project.trainingdiary.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
  public CustomResponse<?> checkDuplicateAndSendVerification(
      @RequestBody @Valid SendVerificationAndCheckDuplicateRequestDto dto
  ) {
    userService.checkDuplicateEmailAndSendVerification(dto);
    return CustomResponse.success(SuccessMessage.SENT_VERIFICATION_SUCCESS);
  }

  @PostMapping("/check-verification-code")
  public CustomResponse<?> verifyCode(
      @RequestBody @Valid VerifyCodeRequestDto dto
  ) {
    userService.checkVerificationCode(dto);
    return CustomResponse.success(SuccessMessage.VERIFICATION_SUCCESS);
  }

  @PostMapping("/sign-up")
  public CustomResponse<?> signUp(
      @RequestBody @Valid SignUpRequestDto dto, HttpServletResponse response
  ) {
    userService.signUp(dto, response);
    return CustomResponse.success(SuccessMessage.SIGN_UP_SUCCESS);
  }

  @PostMapping("/sign-in")
  public CustomResponse<SignInResponseDto> signIn(
      @RequestBody @Valid SignInRequestDto dto, HttpServletResponse response
  ) {
    SignInResponseDto signInResponse = userService.signIn(dto, response);
    return CustomResponse.success(signInResponse, SuccessMessage.SIGN_IN_SUCCESS);
  }

  @PostMapping("/sign-out")
  public CustomResponse<?> signOut(
      HttpServletRequest request, HttpServletResponse response
  ) {
    userService.signOut(request, response);
    return CustomResponse.success(SuccessMessage.SIGN_OUT_SUCCESS);
  }

  @GetMapping("/{id}")
  public CustomResponse<MemberInfoResponseDto> viewUserInfo(
      @PathVariable Long id
  ) {
    MemberInfoResponseDto user = userService.memberInfo(id);
    return CustomResponse.success(user, SuccessMessage.VIEW_USER_INFO_SUCCESS);
  }
}
