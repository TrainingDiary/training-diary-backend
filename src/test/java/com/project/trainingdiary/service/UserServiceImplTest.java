package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.SendVerificationAndCheckDuplicateRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.VerificationEntity;
import com.project.trainingdiary.exception.impl.TraineeEmailDuplicateException;
import com.project.trainingdiary.exception.impl.TrainerEmailDuplicateException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.exception.impl.VerificationCodeExpiredException;
import com.project.trainingdiary.provider.EmailProvider;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.VerificationRepository;
import com.project.trainingdiary.service.impl.UserServiceImpl;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

  @Mock
  private TraineeRepository traineeRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private VerificationRepository verificationRepository;

  @InjectMocks
  private UserServiceImpl userService;

  @Mock
  private EmailProvider emailProvider;

  private SendVerificationAndCheckDuplicateRequestDto sendDto;
  private VerifyCodeRequestDto verifyDto;
  private VerificationEntity verificationEntity;

  @Captor
  private ArgumentCaptor<String> verificationCodeCaptor;

  @BeforeEach
  void setUp() {

    sendDto = new SendVerificationAndCheckDuplicateRequestDto();
    sendDto.setEmail("test@example.com");

    verifyDto = new VerifyCodeRequestDto();
    verifyDto.setEmail("test@example.com");
    verifyDto.setVerificationCode("123456");

    verificationEntity = new VerificationEntity();
    verificationEntity.setEmail("test@example.com");
    verificationEntity.setVerificationCode("123456");
    verificationEntity.setExpiredAt(LocalDateTime.now().plusMinutes(10));
  }

  @Test
  @DisplayName("이메일이 Trainee에 존재할 때 예외 발생")
  void checkDuplicateEmailThrowsExceptionWhenEmailExistsInTrainee() {
    when(traineeRepository.findByEmail(sendDto.getEmail())).thenReturn(
        Optional.of(new TraineeEntity()));

    assertThrows(TraineeEmailDuplicateException.class,
        () -> userService.checkDuplicateEmailAndSendVerification(sendDto));
  }

  @Test
  @DisplayName("이메일이 Trainer에 존재할 때 예외 발생")
  void checkDuplicateEmailThrowsExceptionWhenEmailExistsInTrainer() {
    when(trainerRepository.findByEmail(sendDto.getEmail())).thenReturn(
        Optional.of(new TrainerEntity()));

    assertThrows(TrainerEmailDuplicateException.class,
        () -> userService.checkDuplicateEmailAndSendVerification(sendDto));
  }

  @Test
  @DisplayName("이메일이 존재하지 않을 때 성공")
  void checkDuplicateEmailSuccessWhenEmailDoesNotExist() {
    when(traineeRepository.findByEmail(sendDto.getEmail())).thenReturn(Optional.empty());
    when(trainerRepository.findByEmail(sendDto.getEmail())).thenReturn(Optional.empty());

    doNothing().when(emailProvider)
        .sendVerificationEmail(eq(sendDto.getEmail()), verificationCodeCaptor.capture());

    userService.checkDuplicateEmailAndSendVerification(sendDto);

    verify(verificationRepository, times(1)).save(any(VerificationEntity.class));
    verify(emailProvider, times(1)).sendVerificationEmail(eq(sendDto.getEmail()), anyString());

    String capturedVerificationNumber = verificationCodeCaptor.getValue();
    assert capturedVerificationNumber != null;
  }

  @Test
  @DisplayName("인증 코드 만료 시 예외 발생")
  void checkVerificationCodeThrowsExceptionWhenCodeExpired() {
    verificationEntity.setExpiredAt(LocalDateTime.now().minusMinutes(1));
    when(verificationRepository.findByEmail(verifyDto.getEmail()))
        .thenReturn(Optional.of(verificationEntity));

    assertThrows(VerificationCodeExpiredException.class,
        () -> userService.checkVerificationCode(verifyDto));
  }

  @Test
  @DisplayName("사용자를 찾을 수 없을 때 예외 발생")
  void checkVerificationCodeThrowsExceptionWhenUserNotFound() {
    when(verificationRepository.findByEmail(verifyDto.getEmail()))
        .thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.checkVerificationCode(verifyDto));
  }

  @Test
  @DisplayName("인증 코드 검증 성공")
  void checkVerificationCodeSuccess() {
    when(verificationRepository.findByEmail(verifyDto.getEmail()))
        .thenReturn(Optional.of(verificationEntity));

    userService.checkVerificationCode(verifyDto);
  }
}
