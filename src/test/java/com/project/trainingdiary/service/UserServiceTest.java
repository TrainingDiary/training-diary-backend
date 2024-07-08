package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.SendVerificationAndCheckDuplicateRequestDto;
import com.project.trainingdiary.dto.request.SignInRequestDto;
import com.project.trainingdiary.dto.request.SignOutRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;
import com.project.trainingdiary.dto.response.SignInResponseDto;
import com.project.trainingdiary.entity.BlacklistedTokenEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.VerificationEntity;
import com.project.trainingdiary.exception.impl.TraineeEmailDuplicateException;
import com.project.trainingdiary.exception.impl.TrainerEmailDuplicateException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.exception.impl.VerificationCodeExpiredException;
import com.project.trainingdiary.exception.impl.WrongPasswordException;
import com.project.trainingdiary.model.UserRoleType;
import com.project.trainingdiary.provider.EmailProvider;
import com.project.trainingdiary.provider.TokenProvider;
import com.project.trainingdiary.repository.BlacklistRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.VerificationRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private TraineeRepository traineeRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private VerificationRepository verificationRepository;

  @Mock
  private BlacklistRepository blacklistRepository;

  @Mock
  private EmailProvider emailProvider;

  @Mock
  private TokenProvider tokenProvider;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  private SendVerificationAndCheckDuplicateRequestDto sendDto;
  private VerifyCodeRequestDto verifyDto;
  private VerificationEntity verificationEntity;
  private TraineeEntity traineeEntity;
  private TrainerEntity trainerEntity;

  @Captor
  private ArgumentCaptor<VerificationEntity> verificationEntityCaptor;

  @Captor
  private ArgumentCaptor<BlacklistedTokenEntity> blacklistedTokenEntityCaptor;

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

    traineeEntity = new TraineeEntity();
    traineeEntity.setEmail("trainee@example.com");
    traineeEntity.setPassword("password");
    traineeEntity.setRole(UserRoleType.TRAINEE);

    trainerEntity = new TrainerEntity();
    trainerEntity.setEmail("trainer@example.com");
    trainerEntity.setPassword("password");
    trainerEntity.setRole(UserRoleType.TRAINER);
  }

  @Test
  @DisplayName("이메일이 Trainee에 존재할 때 예외 발생")
  void checkDuplicateEmailThrowsExceptionWhenEmailExistsInTrainee() {
    when(traineeRepository.findByEmail(sendDto.getEmail())).thenReturn(Optional.of(traineeEntity));

    assertThrows(TraineeEmailDuplicateException.class, () -> userService.checkDuplicateEmailAndSendVerification(sendDto));
  }

  @Test
  @DisplayName("이메일이 Trainer에 존재할 때 예외 발생")
  void checkDuplicateEmailThrowsExceptionWhenEmailExistsInTrainer() {
    when(trainerRepository.findByEmail(sendDto.getEmail())).thenReturn(Optional.of(trainerEntity));

    assertThrows(TrainerEmailDuplicateException.class, () -> userService.checkDuplicateEmailAndSendVerification(sendDto));
  }

  @Test
  @DisplayName("이메일이 존재하지 않을 때 성공")
  void checkDuplicateEmailSuccessWhenEmailDoesNotExist() {
    when(traineeRepository.findByEmail(sendDto.getEmail())).thenReturn(Optional.empty());
    when(trainerRepository.findByEmail(sendDto.getEmail())).thenReturn(Optional.empty());

    doNothing().when(emailProvider).sendVerificationEmail(eq(sendDto.getEmail()), anyString());

    userService.checkDuplicateEmailAndSendVerification(sendDto);

    verify(verificationRepository, times(1)).save(verificationEntityCaptor.capture());
    verify(emailProvider, times(1)).sendVerificationEmail(eq(sendDto.getEmail()), anyString());

    VerificationEntity capturedVerificationEntity = verificationEntityCaptor.getValue();
    assertNotNull(capturedVerificationEntity.getVerificationCode());
  }

  @Test
  @DisplayName("인증 코드 만료 시 예외 발생")
  void checkVerificationCodeThrowsExceptionWhenCodeExpired() {
    verificationEntity.setExpiredAt(LocalDateTime.now().minusMinutes(1));
    when(verificationRepository.findByEmail(verifyDto.getEmail())).thenReturn(Optional.of(verificationEntity));

    assertThrows(VerificationCodeExpiredException.class, () -> userService.checkVerificationCode(verifyDto));
  }

  @Test
  @DisplayName("사용자를 찾을 수 없을 때 예외 발생")
  void checkVerificationCodeThrowsExceptionWhenUserNotFound() {
    when(verificationRepository.findByEmail(verifyDto.getEmail())).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.checkVerificationCode(verifyDto));
  }

  @Test
  @DisplayName("인증 코드 검증 성공")
  void checkVerificationCodeSuccess() {
    when(verificationRepository.findByEmail(verifyDto.getEmail())).thenReturn(Optional.of(verificationEntity));

    userService.checkVerificationCode(verifyDto);
  }

  @Test
  @DisplayName("로그인 성공")
  void signInSuccess() {
    SignInRequestDto signInDto = new SignInRequestDto();
    signInDto.setEmail("trainee@example.com");
    signInDto.setPassword("password");

    when(traineeRepository.findByEmail(signInDto.getEmail())).thenReturn(Optional.of(traineeEntity));
    when(passwordEncoder.matches(signInDto.getPassword(), traineeEntity.getPassword())).thenReturn(true);
    when(tokenProvider.createToken(traineeEntity.getEmail())).thenReturn("testToken");

    SignInResponseDto response = userService.signIn(signInDto);

    assertEquals("accessToken", response.getAccessToken());
    assertEquals("testToken", response.getRefreshToken());
    assertEquals(traineeEntity.getEmail(), response.getEmail());
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void signInFailWrongPassword() {
    SignInRequestDto signInDto = new SignInRequestDto();
    signInDto.setEmail("trainee@example.com");
    signInDto.setPassword("wrongPassword");

    when(traineeRepository.findByEmail(signInDto.getEmail())).thenReturn(Optional.of(traineeEntity));
    when(passwordEncoder.matches(signInDto.getPassword(), traineeEntity.getPassword())).thenReturn(false);

    assertThrows(WrongPasswordException.class, () -> userService.signIn(signInDto));
  }

  @Test
  @DisplayName("로그인 실패 - 사용자 없음")
  void signInFailUserNotFound() {
    SignInRequestDto signInDto = new SignInRequestDto();
    signInDto.setEmail("nonexistent@example.com");
    signInDto.setPassword("password");

    when(traineeRepository.findByEmail(signInDto.getEmail())).thenReturn(Optional.empty());
    when(trainerRepository.findByEmail(signInDto.getEmail())).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.signIn(signInDto));
  }


  @Test
  @DisplayName("로그아웃 시 토큰 블랙리스트 등록 성공")
  void signOutSuccess() {
    String token = "testToken";
    SignOutRequestDto signOutDto = new SignOutRequestDto();
    signOutDto.setToken(token);

    LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);
    when(tokenProvider.getExpiryDateFromToken(token)).thenReturn(expiryDate);

    userService.signOut(signOutDto);

    verify(blacklistRepository, times(1)).save(blacklistedTokenEntityCaptor.capture());

    BlacklistedTokenEntity capturedBlacklistedTokenEntity = blacklistedTokenEntityCaptor.getValue();
    assertEquals(token, capturedBlacklistedTokenEntity.getToken());
    assertEquals(expiryDate, capturedBlacklistedTokenEntity.getExpiryDate());
  }
}
