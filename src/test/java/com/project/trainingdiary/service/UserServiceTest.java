package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.SendVerificationAndCheckDuplicateRequestDto;
import com.project.trainingdiary.dto.request.SignInRequestDto;
import com.project.trainingdiary.dto.request.SignUpRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;
import com.project.trainingdiary.dto.response.SignInResponseDto;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.VerificationEntity;
import com.project.trainingdiary.exception.impl.PasswordMismatchedException;
import com.project.trainingdiary.exception.impl.TraineeEmailDuplicateException;
import com.project.trainingdiary.exception.impl.TrainerEmailDuplicateException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.exception.impl.VerificationCodeExpiredException;
import com.project.trainingdiary.exception.impl.VerificationCodeNotMatchedException;
import com.project.trainingdiary.exception.impl.VerificationCodeNotYetVerifiedException;
import com.project.trainingdiary.exception.impl.WrongPasswordException;
import com.project.trainingdiary.model.UserRoleType;
import com.project.trainingdiary.provider.CookieProvider;
import com.project.trainingdiary.provider.EmailProvider;
import com.project.trainingdiary.provider.TokenProvider;
import com.project.trainingdiary.repository.RedisTokenRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.VerificationRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
  private RedisTokenRepository redisTokenRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private VerificationRepository verificationRepository;

  @Mock
  private EmailProvider emailProvider;

  @Mock
  private TokenProvider tokenProvider;

  @Mock
  private CookieProvider cookieProvider;

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

    assertThrows(TraineeEmailDuplicateException.class,
        () -> userService.checkDuplicateEmailAndSendVerification(sendDto));
  }

  @Test
  @DisplayName("이메일이 Trainer에 존재할 때 예외 발생")
  void checkDuplicateEmailThrowsExceptionWhenEmailExistsInTrainer() {
    when(trainerRepository.findByEmail(sendDto.getEmail())).thenReturn(Optional.of(trainerEntity));

    assertThrows(TrainerEmailDuplicateException.class,
        () -> userService.checkDuplicateEmailAndSendVerification(sendDto));
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
    when(verificationRepository.findByEmail(verifyDto.getEmail())).thenReturn(
        Optional.of(verificationEntity));

    assertThrows(VerificationCodeExpiredException.class,
        () -> userService.checkVerificationCode(verifyDto));
  }

  @Test
  @DisplayName("인증 코드 검증 실패 - 코드 불일치")
  void checkVerificationCodeFailIncorrectCode() {
    verifyDto.setVerificationCode("wrongCode");

    when(verificationRepository.findByEmail(verifyDto.getEmail())).thenReturn(
        Optional.of(verificationEntity));

    assertThrows(VerificationCodeNotMatchedException.class,
        () -> userService.checkVerificationCode(verifyDto));
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
    when(verificationRepository.findByEmail(verifyDto.getEmail())).thenReturn(
        Optional.of(verificationEntity));

    userService.checkVerificationCode(verifyDto);

    verify(verificationRepository, times(1)).save(verificationEntityCaptor.capture());
    VerificationEntity capturedVerificationEntity = verificationEntityCaptor.getValue();
    assertNotNull(capturedVerificationEntity);
    assertTrue(capturedVerificationEntity.isVerified());
    assertNull(capturedVerificationEntity.getExpiredAt());
  }

  @Test
  @DisplayName("인증 코드 만료 후에도 검증됨 - 예외 발생하지 않음")
  void checkVerificationCodeSuccessEvenIfExpiredAfterVerification() {
    verificationEntity.setVerified(true); // Already verified
    verificationEntity.setExpiredAt(LocalDateTime.now().minusMinutes(1)); // Expired

    when(verificationRepository.findByEmail(verifyDto.getEmail())).thenReturn(
        Optional.of(verificationEntity));

    userService.checkVerificationCode(verifyDto);

    verify(verificationRepository, times(1)).save(verificationEntityCaptor.capture());
    VerificationEntity capturedVerificationEntity = verificationEntityCaptor.getValue();
    assertNotNull(capturedVerificationEntity);
    assertTrue(capturedVerificationEntity.isVerified());
    assertNull(capturedVerificationEntity.getExpiredAt());
  }

  @Test
  @DisplayName("회원가입 실패 - 인증 코드 검증되지 않음")
  void signUpFailWithoutVerification() {
    SignUpRequestDto signUpDto = new SignUpRequestDto();
    signUpDto.setEmail("new@example.com");
    signUpDto.setPassword("password");
    signUpDto.setConfirmPassword("password");
    signUpDto.setRole(UserRoleType.TRAINEE);

    VerificationEntity verificationEntity = new VerificationEntity();
    verificationEntity.setEmail("new@example.com");
    verificationEntity.setVerificationCode("123456");
    verificationEntity.setExpiredAt(LocalDateTime.now().plusMinutes(5));
    verificationEntity.setVerified(false); // Not verified

    when(verificationRepository.findByEmail(signUpDto.getEmail()))
        .thenReturn(Optional.of(verificationEntity));

    assertThrows(
        VerificationCodeNotYetVerifiedException.class, () -> userService.signUp(signUpDto, null));
  }

  @Test
  @DisplayName("회원가입 실패 - 비밀번호 불일치")
  void signUpFailPasswordMismatch() {
    // given
    SignUpRequestDto signUpDto = new SignUpRequestDto();
    signUpDto.setEmail("new@example.com");
    signUpDto.setPassword("password");
    signUpDto.setConfirmPassword("differentPassword");
    signUpDto.setRole(UserRoleType.TRAINEE);

    VerificationEntity verificationEntity = new VerificationEntity();
    verificationEntity.setEmail("new@example.com");
    verificationEntity.setVerificationCode("123456");
    verificationEntity.setExpiredAt(LocalDateTime.now().plusMinutes(5));

    when(verificationRepository.findByEmail(signUpDto.getEmail()))
        .thenReturn(java.util.Optional.of(verificationEntity));

    // when / then
    assertThrows(PasswordMismatchedException.class, () -> userService.signUp(signUpDto, null));
  }

  @Test
  @DisplayName("로그인 성공")
  void signInSuccess() {
    SignInRequestDto signInDto = new SignInRequestDto();
    signInDto.setEmail("trainee@example.com");
    signInDto.setPassword("password");

    LocalDateTime accessTokenExpiryDate = Instant.ofEpochMilli(System.currentTimeMillis() + 3600000)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime(); // 1 hour later

    LocalDateTime refreshTokenExpiryDate = Instant.ofEpochMilli(
            System.currentTimeMillis() + 604800000)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime(); // 7 days later

    when(traineeRepository.findByEmail(signInDto.getEmail())).thenReturn(
        Optional.of(traineeEntity));
    when(passwordEncoder.matches(signInDto.getPassword(), traineeEntity.getPassword())).thenReturn(
        true);
    when(tokenProvider.createAccessToken(traineeEntity.getEmail())).thenReturn("accessToken");
    when(tokenProvider.createRefreshToken(traineeEntity.getEmail())).thenReturn("refreshToken");
    when(tokenProvider.getExpiryDateFromToken("accessToken")).thenReturn(accessTokenExpiryDate);
    when(tokenProvider.getExpiryDateFromToken("refreshToken")).thenReturn(refreshTokenExpiryDate);

    HttpServletResponse response = mock(HttpServletResponse.class);

    SignInResponseDto responseDto = userService.signIn(signInDto, response);

    assertEquals("trainee@example.com", responseDto.getEmail());
    assertEquals("[ROLE_TRAINEE]", responseDto.getRole());
    assertEquals(accessTokenExpiryDate, tokenProvider.getExpiryDateFromToken("accessToken"));
    assertEquals(refreshTokenExpiryDate, tokenProvider.getExpiryDateFromToken("refreshToken"));

  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void signInFailWrongPassword() {
    SignInRequestDto signInDto = new SignInRequestDto();
    signInDto.setEmail("trainee@example.com");
    signInDto.setPassword("wrongPassword");

    when(traineeRepository.findByEmail(signInDto.getEmail())).thenReturn(
        Optional.of(traineeEntity));
    when(passwordEncoder.matches(signInDto.getPassword(), traineeEntity.getPassword())).thenReturn(
        false);

    assertThrows(WrongPasswordException.class,
        () -> userService.signIn(signInDto, mock(HttpServletResponse.class)));
  }

  @Test
  @DisplayName("로그인 실패 - 사용자 없음")
  void signInFailUserNotFound() {
    SignInRequestDto signInDto = new SignInRequestDto();
    signInDto.setEmail("nonexistent@example.com");
    signInDto.setPassword("password");

    when(traineeRepository.findByEmail(signInDto.getEmail())).thenReturn(Optional.empty());
    when(trainerRepository.findByEmail(signInDto.getEmail())).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class,
        () -> userService.signIn(signInDto, mock(HttpServletResponse.class)));
  }

  @Test
  @DisplayName("로그아웃 할떄 블랙리스트 토큰에 저장 및 쿠키 삭제 성공")
  void signOutSuccess() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    Cookie accessTokenCookie = new Cookie("Access-Token", "accessToken");
    Cookie refreshTokenCookie = new Cookie("Refresh-Token", "refreshToken");

    when(cookieProvider.getCookie(request, "Access-Token")).thenReturn(accessTokenCookie);
    when(cookieProvider.getCookie(request, "Refresh-Token")).thenReturn(refreshTokenCookie);

    when(tokenProvider.validateToken("accessToken")).thenReturn(true);
    when(tokenProvider.validateToken("refreshToken")).thenReturn(true);

    userService.signOut(request, response);

    verify(tokenProvider, times(1)).blacklistToken("accessToken");
    verify(tokenProvider, times(1)).blacklistToken("refreshToken");
    verify(cookieProvider, times(1)).clearCookie(response, "Access-Token");
    verify(cookieProvider, times(1)).clearCookie(response, "Refresh-Token");
  }

  @Test
  @DisplayName("로그아웃 실패 - 쿠키가 없음")
  void signOutFailNoCookies() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    userService.signOut(request, response);

    verify(tokenProvider, times(0)).blacklistToken(anyString());
    verify(cookieProvider, times(1)).clearCookie(response, "Access-Token");
    verify(cookieProvider, times(1)).clearCookie(response, "Refresh-Token");
  }

  @Test
  @DisplayName("로그아웃 실패 - 유효하지 않은 토큰")
  void signOutFailInvalidTokens() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    Cookie accessTokenCookie = new Cookie("Access-Token", "invalidAccessToken");
    Cookie refreshTokenCookie = new Cookie("Refresh-Token", "invalidRefreshToken");

    when(request.getCookies()).thenReturn(new Cookie[]{accessTokenCookie, refreshTokenCookie});
    when(cookieProvider.getCookie(request, "Access-Token")).thenCallRealMethod();
    when(cookieProvider.getCookie(request, "Refresh-Token")).thenCallRealMethod();
    when(tokenProvider.validateToken("invalidAccessToken")).thenReturn(false);
    when(tokenProvider.validateToken("invalidRefreshToken")).thenReturn(false);

    userService.signOut(request, response);

    verify(tokenProvider, times(0)).blacklistToken("invalidAccessToken");
    verify(tokenProvider, times(0)).blacklistToken("invalidRefreshToken");
    verify(cookieProvider, times(1)).clearCookie(response, "Access-Token");
    verify(cookieProvider, times(1)).clearCookie(response, "Refresh-Token");
  }
}