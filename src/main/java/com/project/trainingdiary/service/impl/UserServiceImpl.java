package com.project.trainingdiary.service.impl;

import com.project.trainingdiary.dto.request.EmailDuplicateCheckRequestDto;
import com.project.trainingdiary.dto.request.SendVerificationEmailRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;
import com.project.trainingdiary.entity.VerificationEntity;
import com.project.trainingdiary.exception.impl.TraineeEmailDuplicateException;
import com.project.trainingdiary.exception.impl.TrainerEmailDuplicateException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.exception.impl.VerificationCodeExpiredException;
import com.project.trainingdiary.exception.impl.VerificationCodeNotMatchedException;
import com.project.trainingdiary.util.VerificationCodeGeneratorUtil;
import com.project.trainingdiary.provider.EmailProvider;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.VerificationRepository;
import com.project.trainingdiary.service.UserService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;
  private final VerificationRepository verificationRepository;

  private final EmailProvider emailProvider;

  @Override
  public void checkDuplicateEmail(EmailDuplicateCheckRequestDto dto) {

    traineeRepository.findByEmail(dto.getEmail())
        .ifPresent(user -> {
          throw new TraineeEmailDuplicateException();
        });

    trainerRepository.findByEmail(dto.getEmail())
        .ifPresent(user -> {
          throw new TrainerEmailDuplicateException();
        });
  }

  @Override
  @Transactional
  public void sendVerificationEmail(SendVerificationEmailRequestDto dto) {

    verificationRepository.findByEmail(dto.getEmail())
        .ifPresent(user -> {
          throw new UserNotFoundException();
        });

    String verificationCode = VerificationCodeGeneratorUtil.generateVerificationCode();
    emailProvider.sendVerificationEmail(dto.getEmail(), (verificationCode));

    VerificationEntity verificationEntity = VerificationEntity.of(dto.getEmail(),
        verificationCode);
    verificationRepository.save(verificationEntity);
  }

  @Override
  public void checkVerificationCode(VerifyCodeRequestDto dto) {

    VerificationEntity verificationEntity = verificationRepository.findByEmail(dto.getEmail())
        .orElseThrow(UserNotFoundException::new);

    if (!verificationEntity.getVerificationCode().equals(dto.getVerificationCode())) {
      throw new VerificationCodeNotMatchedException();
    }

    if (verificationEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
      throw new VerificationCodeExpiredException();
    }
  }
}
