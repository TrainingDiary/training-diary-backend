package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.SendVerificationAndCheckDuplicateRequestDto;
import com.project.trainingdiary.dto.request.SignInRequestDto;
import com.project.trainingdiary.dto.request.SignUpRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;
import com.project.trainingdiary.dto.response.MemberInfoResponseDto;
import com.project.trainingdiary.dto.response.SignInResponseDto;
import com.project.trainingdiary.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

  @PostMapping("/check-duplicate-and-send-verification")
  public ResponseEntity<Void> checkDuplicateAndSendVerification(
      @RequestBody @Valid SendVerificationAndCheckDuplicateRequestDto dto
  ) {
    userService.checkDuplicateEmailAndSendVerification(dto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/check-verification-code")
  public ResponseEntity<Void> verifyCode(
      @RequestBody @Valid VerifyCodeRequestDto dto
  ) {
    userService.checkVerificationCode(dto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/sign-up")
  public ResponseEntity<Void> signUp(
      @RequestBody @Valid SignUpRequestDto dto, HttpServletResponse response
  ) {
    userService.signUp(dto, response);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/sign-in")
  public ResponseEntity<SignInResponseDto> signIn(
      @RequestBody @Valid SignInRequestDto dto, HttpServletResponse response
  ) {
    SignInResponseDto signInResponse = userService.signIn(dto, response);
    return ResponseEntity.ok(signInResponse);
  }

  @PostMapping("/sign-out")
  public ResponseEntity<Void> signOut(
      HttpServletRequest request, HttpServletResponse response
  ) {
    userService.signOut(request, response);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/info")
  public ResponseEntity<MemberInfoResponseDto> userInfo() {
    MemberInfoResponseDto user = userService.memberInfo();
    return ResponseEntity.ok(user);
  }
}
