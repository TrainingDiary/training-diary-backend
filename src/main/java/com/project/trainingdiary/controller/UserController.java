package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.user.SendVerificationAndCheckDuplicateRequestDto;
import com.project.trainingdiary.dto.request.user.SignInRequestDto;
import com.project.trainingdiary.dto.request.user.SignUpRequestDto;
import com.project.trainingdiary.dto.request.user.VerifyCodeRequestDto;
import com.project.trainingdiary.dto.response.user.MemberInfoResponseDto;
import com.project.trainingdiary.dto.response.user.SignInResponseDto;
import com.project.trainingdiary.dto.response.user.SignUpResponseDto;
import com.project.trainingdiary.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "1 - User API", description = "사용자 관리를 위한 API")
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(
      summary = "이메일 중복 확인 및 인증 코드 발송",
      description = "이메일 중복 여부를 확인하고 인증 코드를 발송합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "409", description = "이미 등록된 이메일입니다.", content = @Content)
  })
  @PostMapping("/check-duplicate-and-send-verification")
  public ResponseEntity<Void> checkDuplicateAndSendVerification(
      @RequestBody @Valid SendVerificationAndCheckDuplicateRequestDto dto
  ) {
    userService.checkDuplicateEmailAndSendVerification(dto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "인증 코드 확인",
      description = "사용자 이메일로 전송된 인증 코드를 확인합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "인증 코드가 일치하지 않습니다.", content = @Content),
      @ApiResponse(responseCode = "406", description = "인증 코드가 만료 되었습니다.", content = @Content)
  })
  @PostMapping("/check-verification-code")
  public ResponseEntity<Void> verifyCode(
      @RequestBody @Valid VerifyCodeRequestDto dto
  ) {
    userService.checkVerificationCode(dto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "회원 가입",
      description = "새로운 사용자를 등록합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "인증 코드가 아직 검증되지 않았습니다.", content = @Content),
      @ApiResponse(responseCode = "403", description = "비밀번호가 일치하지 않습니다.", content = @Content),
      @ApiResponse(responseCode = "409", description = "이미 등록된 이메일입니다.", content = @Content)
  })
  @PostMapping("/sign-up")
  public ResponseEntity<SignUpResponseDto> signUp(
      @RequestBody @Valid SignUpRequestDto dto, HttpServletRequest request,
      HttpServletResponse response
  ) {
    return ResponseEntity.ok(userService.signUp(dto, request, response));
  }

  @Operation(
      summary = "로그인",
      description = "사용자를 인증하고 토큰을 반환합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "400", description = "가입된 이메일이 아닙니다.", content = @Content),
      @ApiResponse(responseCode = "400", description = "비밀번호가 틀렸습니다.", content = @Content)})

  @PostMapping("/sign-in")
  public ResponseEntity<SignInResponseDto> signIn(
      @RequestBody @Valid SignInRequestDto dto, HttpServletRequest request,
      HttpServletResponse response
  ) {
    return ResponseEntity.ok(userService.signIn(dto, request, response));
  }

  @Operation(
      summary = "로그아웃",
      description = "사용자를 로그아웃합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공")
  })
  @PostMapping("/sign-out")
  public ResponseEntity<Void> signOut(
      HttpServletRequest request, HttpServletResponse response
  ) {
    userService.signOut(request, response);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "사용자 정보 조회",
      description = "인증된 사용자의 정보를 조회합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 회원입니다.", content = @Content)})
  @GetMapping("/info")
  public ResponseEntity<MemberInfoResponseDto> userInfo() {
    return ResponseEntity.ok(userService.memberInfo());
  }
}