package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.SendVerificationAndCheckDuplicateRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;

public interface UserService {

  void checkVerificationCode(VerifyCodeRequestDto dto);

  void checkDuplicateEmailAndSendVerification(SendVerificationAndCheckDuplicateRequestDto dto);
}
