package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.response.CustomResponse;
import com.project.trainingdiary.dto.response.ExampleResponseDto;
import com.project.trainingdiary.exception.GlobalException;
import com.project.trainingdiary.model.SuccessMessage;
import com.project.trainingdiary.model.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// CustomResponse를 보여주기 위한 Sample 응답입니다.
// dto를 넣어서 CustomResponse를 만들면,
// 아래와 같이 statusCode와 message를 추가하고, data에 dto를 넣어 응답을 생성합니다.
// {
//  "data": {
//    "name": "sample",
//    "age": 30
//  },
//  "message": "성공",
//  "statusCode": 200
// }

@RestController
public class SampleController {

  private static final Logger log = LoggerFactory.getLogger(SampleController.class);

  @GetMapping("/sample/success")
  public CustomResponse<ExampleResponseDto> sampleSuccess() {
    ExampleResponseDto dto = new ExampleResponseDto("sample", 30);
    return CustomResponse.success(dto);
  }

  @GetMapping("/sample/success/simple")
  public CustomResponse<ExampleResponseDto> sampleSuccessSimple() {
    ExampleResponseDto dto = new ExampleResponseDto("sample", 30);
    return CustomResponse.success(dto, SuccessMessage.SIGN_UP_SUCCESS);
  }

  @GetMapping("/sample/exception")
  public CustomResponse<Void> sampleException() {
    throw new GlobalException(HttpStatus.NOT_FOUND, "리소스가 없습니다.");
  }

  @GetMapping("api/test/protected")
  public CustomResponse<String> protectedEndpoint(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    log.info("User: {}", userPrincipal.getUsername());
    return CustomResponse.success("인증 권한 있습니다");
  }

  @PreAuthorize("hasRole('TRAINER')")
  @GetMapping("api/test/trainer")
  public CustomResponse<String> preAuthorizeTrainer(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    log.info("User: {}", userPrincipal.getAuthorities());
    return CustomResponse.success("인증 권한 있습니다");
  }

  @PreAuthorize("hasRole('TRAINEE')")
  @GetMapping("api/test/trainee")
  public CustomResponse<String> preAuthorizeTrainee(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    log.info("User: {}", userPrincipal.getAuthorities());
    return CustomResponse.success("인증 권한 있습니다");
  }
}
