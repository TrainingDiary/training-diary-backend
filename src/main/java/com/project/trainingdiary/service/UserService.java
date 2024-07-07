package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.SendVerificationAndCheckDuplicateRequestDto;
import com.project.trainingdiary.dto.request.SignInRequestDto;
import com.project.trainingdiary.dto.request.SignOutRequestDto;
import com.project.trainingdiary.dto.request.SignUpRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;
import com.project.trainingdiary.dto.response.SignInResponseDto;

public interface UserService {

  void checkVerificationCode(VerifyCodeRequestDto dto);

  void checkDuplicateEmailAndSendVerification(SendVerificationAndCheckDuplicateRequestDto dto);

  void signUp(SignUpRequestDto dto);

  SignInResponseDto signIn(SignInRequestDto dto);

  void signOut(SignOutRequestDto dto);
}
