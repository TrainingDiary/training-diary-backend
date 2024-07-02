package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.EmailDuplicateCheckRequestDto;
import com.project.trainingdiary.dto.request.SendVerificationEmailRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;

public interface UserService {

  void checkDuplicateEmail(EmailDuplicateCheckRequestDto dto);

  void sendVerificationEmail(SendVerificationEmailRequestDto dto);

  void checkVerificationCode(VerifyCodeRequestDto dto);
}
