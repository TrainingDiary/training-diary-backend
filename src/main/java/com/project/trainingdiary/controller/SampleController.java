package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.dto.response.ExampleResponseDto;
import com.project.trainingdiary.exception.GlobalException;
import com.project.trainingdiary.model.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// CommonResponse를 보여주기 위한 Sample 응답입니다.
// dto를 넣어서 CommonResponse를 만들면,
// 아래와 같이 statusCode와 message를 추가하고, data에 dto를 넣어 응답을 생성합니다.
// {
//  "data": {
//    "name": "sample",
//    "age": 30
//  },
//  "message": "성공",
//  "statusCode": 200
// }
//
// 이 SampleController 파일은 1주일 후(2024.7.9.) 삭제하겠습니다.

@RestController
public class SampleController {

  private static final Logger log = LoggerFactory.getLogger(SampleController.class);

  @GetMapping("/sample/success")
  public CommonResponse<?> sampleSuccess() {
    ExampleResponseDto dto = new ExampleResponseDto("sample", 30);
    return new CommonResponse<>(dto, HttpStatus.OK, "성공입니다.");
  }

  @GetMapping("/sample/success/simple")
  public CommonResponse<?> sampleSuccessSimple() {
    ExampleResponseDto dto = new ExampleResponseDto("sample", 30);
    return CommonResponse.success(dto);
  }

  @GetMapping("/sample/created")
  public CommonResponse<?> sampleCreated() {
    ExampleResponseDto dto = new ExampleResponseDto("sample", 30);
    return CommonResponse.created(dto);
  }

  @GetMapping("/sample/client-fail")
  public CommonResponse<?> sampleClientFail() {
    return new CommonResponse<>(HttpStatus.NOT_FOUND, "리소스가 없습니다.");
  }

  @GetMapping("/sample/client-fail/simple")
  public CommonResponse<?> sampleClientFailSimple() {
    return CommonResponse.clientFail();
  }

  @GetMapping("/sample/server-fail")
  public CommonResponse<?> sampleServerFail() {
    return CommonResponse.serverFail("내부 서버 에러입니다.");
  }

  @GetMapping("/sample/exception")
  public CommonResponse<?> sampleException() {
    throw new GlobalException(HttpStatus.NOT_FOUND, "리소스가 없습니다.");
  }

  @GetMapping("api/test/protected")
  public CommonResponse<?> protectedEndpoint(@AuthenticationPrincipal UserPrincipal userPrincipal) {
    log.info("User: {}", userPrincipal.getUsername());
    return CommonResponse.success("인증 권한 있습니다");
  }

  @PreAuthorize("hasRole('TRAINER')")
  @GetMapping("api/test/trainer")
  public CommonResponse<?> preAuthorizeTrainer(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    log.info("User: {}", userPrincipal.getAuthorities());
    return CommonResponse.success("인증 권한 있습니다");
  }

  @PreAuthorize("hasRole('TRAINEE')")
  @GetMapping("api/test/trainee")
  public CommonResponse<?> preAuthorizeTrainee(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    log.info("User: {}", userPrincipal.getAuthorities());
    return CommonResponse.success("인증 권한 있습니다");
  }
}
