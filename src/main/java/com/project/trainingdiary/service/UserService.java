package com.project.trainingdiary.service;

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
import com.project.trainingdiary.exception.impl.WrongPasswordException;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.UserRoleType;
import com.project.trainingdiary.provider.CookieProvider;
import com.project.trainingdiary.provider.EmailProvider;
import com.project.trainingdiary.provider.TokenProvider;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.VerificationRepository;
import com.project.trainingdiary.util.VerificationCodeGeneratorUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;
  private final VerificationRepository verificationRepository;
  private final EmailProvider emailProvider;
  private final TokenProvider tokenProvider;
  private final CookieProvider cookieProvider;
  private final PasswordEncoder passwordEncoder;

  private static final String REFRESH_TOKEN_COOKIE_NAME = "Refresh-Token";
  private static final String ACCESS_TOKEN_COOKIE_NAME = "Access-Token";

  /**
   * 이메일 중복을 확인하고 인증 코드를 발송합니다.
   */
  @Transactional
  public void checkDuplicateEmailAndSendVerification(
      SendVerificationAndCheckDuplicateRequestDto dto
  ) {
    validateEmailNotExists(dto.getEmail());
    sendVerificationCode(dto.getEmail());
  }

  /**
   * 인증 코드를 확인합니다.
   */
  public void checkVerificationCode(VerifyCodeRequestDto dto) {
    VerificationEntity verificationEntity = getVerificationEntity(dto.getEmail());
    validateVerificationCode(verificationEntity, dto.getVerificationCode());
  }

  /**
   * 회원가입을 처리합니다.
   */
  @Transactional
  public void signUp(SignUpRequestDto dto, HttpServletResponse response) {
    validateEmailNotExists(dto.getEmail());
    validatePasswordsMatch(dto.getPassword(), dto.getConfirmPassword());
    String encodedPassword = passwordEncoder.encode(dto.getPassword());
    saveUser(dto, encodedPassword);
    generateTokensAndSetCookies(dto.getEmail(), response);
    verificationRepository.deleteByEmail(dto.getEmail());
  }

  /**
   * 로그인 처리를 합니다.
   */
  public SignInResponseDto signIn(SignInRequestDto dto, HttpServletResponse response) {
    UserDetails userDetails = loadUserByUsername(dto.getEmail());
    validatePassword(dto.getPassword(), userDetails.getPassword());
    return generateTokensAndSetCookies(userDetails.getUsername(), response);
  }

  /**
   * 로그아웃 처리를 합니다.
   */
  @Transactional
  public void signOut(HttpServletRequest request, HttpServletResponse response) {
    blacklistAndClearCookies(request, response);
  }

  /**
   * 주어진 이메일이 이미 존재하는지 확인합니다.
   */
  private void validateEmailNotExists(String email) {
    if (traineeRepository.findByEmail(email).isPresent()) {
      throw new TraineeEmailDuplicateException();
    }
    if (trainerRepository.findByEmail(email).isPresent()) {
      throw new TrainerEmailDuplicateException();
    }
  }

  /**
   * 인증 코드를 발송합니다.
   */
  private void sendVerificationCode(String email) {
    String verificationCode = VerificationCodeGeneratorUtil.generateVerificationCode();
    emailProvider.sendVerificationEmail(email, verificationCode);
    verificationRepository.save(VerificationEntity.of(email, verificationCode));
  }

  /**
   * 주어진 비밀번호와 확인 비밀번호가 일치하는지 확인합니다.
   */
  private void validatePasswordsMatch(String password, String confirmPassword) {
    if (!password.equals(confirmPassword)) {
      throw new PasswordMismatchedException();
    }
  }

  /**
   * 인증 코드를 검증합니다.
   */
  private void validateVerificationCode(VerificationEntity verificationEntity,
      String verificationCode) {
    if (!verificationEntity.getVerificationCode().equals(verificationCode)) {
      throw new VerificationCodeNotMatchedException();
    }
    if (verificationEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
      throw new VerificationCodeExpiredException();
    }
  }

  /**
   * 이메일에 대한 인증 엔티티를 가져옵니다.
   */
  private VerificationEntity getVerificationEntity(String email) {
    return verificationRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
  }

  /**
   * 사용자를 저장합니다.
   */
  private void saveUser(SignUpRequestDto dto, String encodedPassword) {
    if (dto.getRole() == UserRoleType.TRAINEE) {
      saveTrainee(dto, encodedPassword);
    } else if (dto.getRole() == UserRoleType.TRAINER) {
      saveTrainer(dto, encodedPassword);
    } else {
      throw new IllegalArgumentException("Invalid role: " + dto.getRole());
    }
  }

  /**
   * 새로운 트레이니를 저장합니다.
   */
  private void saveTrainee(SignUpRequestDto dto, String encodedPassword) {
    TraineeEntity trainee = new TraineeEntity();
    trainee.setName(dto.getName());
    trainee.setEmail(dto.getEmail());
    trainee.setPassword(encodedPassword);
    trainee.setRole(UserRoleType.TRAINEE);
    traineeRepository.save(trainee);
  }

  /**
   * 새로운 트레이너를 저장합니다.
   */
  private void saveTrainer(SignUpRequestDto dto, String encodedPassword) {
    TrainerEntity trainer = new TrainerEntity();
    trainer.setName(dto.getName());
    trainer.setEmail(dto.getEmail());
    trainer.setPassword(encodedPassword);
    trainer.setRole(UserRoleType.TRAINER);
    trainerRepository.save(trainer);
  }

  /**
   * 비밀번호를 검증합니다.
   */
  private void validatePassword(String rawPassword, String encodedPassword) {
    if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
      throw new WrongPasswordException();
    }
  }

  /**
   * 토큰을 생성하고 쿠키를 설정합니다.
   */
  private SignInResponseDto generateTokensAndSetCookies(String username,
      HttpServletResponse response) {
    String accessToken = tokenProvider.createAccessToken(username);
    String refreshToken = tokenProvider.createRefreshToken(username);

    LocalDateTime accessTokenExpiryDate = tokenProvider.getExpiryDateFromToken(accessToken);
    LocalDateTime refreshTokenExpiryDate = tokenProvider.getExpiryDateFromToken(refreshToken);

    cookieProvider.setCookie(response, ACCESS_TOKEN_COOKIE_NAME, accessToken,
        accessTokenExpiryDate);
    cookieProvider.setCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken,
        refreshTokenExpiryDate);

    return new SignInResponseDto(accessToken, refreshToken);
  }

  /**
   * 토큰을 블랙리스트에 추가하고 쿠키를 제거합니다.
   */
  private void blacklistAndClearCookies(HttpServletRequest request, HttpServletResponse response) {
    Cookie accessTokenCookie = cookieProvider.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
    Cookie refreshTokenCookie = cookieProvider.getCookie(request, REFRESH_TOKEN_COOKIE_NAME);

    if (accessTokenCookie != null && tokenProvider.validateToken(accessTokenCookie.getValue())) {
      log.info("블랙리스트에 추가된 접근 토큰: {}", accessTokenCookie.getValue());
      tokenProvider.blacklistToken(accessTokenCookie.getValue());
    }

    if (refreshTokenCookie != null && tokenProvider.validateToken(refreshTokenCookie.getValue())) {
      log.info("블랙리스트에 추가된 리프레시 토큰: {}", refreshTokenCookie.getValue());
      tokenProvider.blacklistToken(refreshTokenCookie.getValue());
    }

    cookieProvider.clearCookie(response, ACCESS_TOKEN_COOKIE_NAME);
    cookieProvider.clearCookie(response, REFRESH_TOKEN_COOKIE_NAME);
  }

  /**
   * 주어진 사용자 이름으로 사용자를 로드합니다.
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    TraineeEntity trainee = traineeRepository.findByEmail(username).orElse(null);

    if (trainee != null) {
      return UserPrincipal.create(trainee);
    }
    TrainerEntity trainer = trainerRepository.findByEmail(username)
        .orElseThrow(UserNotFoundException::new);
    return UserPrincipal.create(trainer);
  }
}