package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.SendVerificationAndCheckDuplicateRequestDto;
import com.project.trainingdiary.dto.request.SignInRequestDto;
import com.project.trainingdiary.dto.request.SignUpRequestDto;
import com.project.trainingdiary.dto.request.VerifyCodeRequestDto;
import com.project.trainingdiary.dto.response.MemberInfoResponseDto;
import com.project.trainingdiary.dto.response.SignInResponseDto;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.VerificationEntity;
import com.project.trainingdiary.exception.impl.PasswordMismatchedException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.exception.impl.VerificationCodeExpiredException;
import com.project.trainingdiary.exception.impl.VerificationCodeNotMatchedException;
import com.project.trainingdiary.exception.impl.WrongPasswordException;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.UserRoleType;
import com.project.trainingdiary.provider.CookieProvider;
import com.project.trainingdiary.provider.EmailProvider;
import com.project.trainingdiary.provider.TokenProvider;
import com.project.trainingdiary.repository.RedisTokenRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.VerificationRepository;
import com.project.trainingdiary.util.VerificationCodeGeneratorUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
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
public class UserService implements UserDetailsService {

  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;
  private final VerificationRepository verificationRepository;
  private final RedisTokenRepository redisTokenRepository;

  private final EmailProvider emailProvider;
  private final TokenProvider tokenProvider;
  private final CookieProvider cookieProvider;

  private final PasswordEncoder passwordEncoder;

  private static final String REFRESH_TOKEN_COOKIE_NAME = "Refresh-Token";
  private static final String ACCESS_TOKEN_COOKIE_NAME = "Access-Token";

  public void checkDuplicateEmailAndSendVerification(
      SendVerificationAndCheckDuplicateRequestDto dto) {
    validateEmailNotExists(dto.getEmail());
    sendVerificationCode(dto.getEmail());
  }

  public void checkVerificationCode(VerifyCodeRequestDto dto) {
    VerificationEntity verificationEntity = getVerificationEntity(dto.getEmail());
    validateVerificationCode(verificationEntity, dto.getVerificationCode());
  }

  @Transactional
  public void signUp(SignUpRequestDto dto, HttpServletResponse response) {
    VerificationEntity verificationEntity = getVerificationEntity(dto.getEmail());
    validateEmailNotExists(dto.getEmail());
    validatePasswordsMatch(dto.getPassword(), dto.getConfirmPassword());
    validateVerificationCode(verificationEntity, dto.getVerificationCode());
    String encodedPassword = passwordEncoder.encode(dto.getPassword());
    saveUser(dto, encodedPassword);
    generateTokensAndSetCookies(dto.getEmail(), response);
    verificationRepository.deleteByEmail(dto.getEmail());
  }

  public SignInResponseDto signIn(SignInRequestDto dto, HttpServletResponse response) {
    UserDetails userDetails = loadUserByUsername(dto.getEmail());
    validatePassword(dto.getPassword(), userDetails.getPassword());
    return generateTokensAndSetCookies(userDetails.getUsername(), response);
  }

  public void signOut(HttpServletRequest request, HttpServletResponse response) {
    blacklistAndClearCookies(request, response);
  }

  private void validateEmailNotExists(String email) {
    if (traineeRepository.findByEmail(email).isPresent() || trainerRepository.findByEmail(email)
        .isPresent()) {
      throw new UserNotFoundException();
    }
  }

  private void sendVerificationCode(String email) {
    String verificationCode = VerificationCodeGeneratorUtil.generateVerificationCode();
    emailProvider.sendVerificationEmail(email, verificationCode);
    verificationRepository.save(VerificationEntity.of(email, verificationCode));
  }

  private void validatePasswordsMatch(String password, String confirmPassword) {
    if (!password.equals(confirmPassword)) {
      throw new PasswordMismatchedException();
    }
  }

  private void validateVerificationCode(VerificationEntity verificationEntity,
      String verificationCode) {
    if (!verificationEntity.getVerificationCode().equals(verificationCode)) {
      throw new VerificationCodeNotMatchedException();
    }
    if (verificationEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
      throw new VerificationCodeExpiredException();
    }
  }

  private VerificationEntity getVerificationEntity(String email) {
    return verificationRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
  }

  private void saveUser(SignUpRequestDto dto, String encodedPassword) {
    if (dto.getRole() == UserRoleType.TRAINEE) {
      saveTrainee(dto, encodedPassword);
    } else if (dto.getRole() == UserRoleType.TRAINER) {
      saveTrainer(dto, encodedPassword);
    } else {
      throw new IllegalArgumentException("Invalid role: " + dto.getRole());
    }
  }

  private void saveTrainee(SignUpRequestDto dto, String encodedPassword) {
    TraineeEntity trainee = TraineeEntity.builder()
        .name(dto.getName())
        .email(dto.getEmail())
        .password(encodedPassword)
        .role(UserRoleType.TRAINEE)
        .build();
    traineeRepository.save(trainee);
  }

  private void saveTrainer(SignUpRequestDto dto, String encodedPassword) {
    TrainerEntity trainer = TrainerEntity.builder()
        .name(dto.getName())
        .email(dto.getEmail())
        .password(encodedPassword)
        .role(UserRoleType.TRAINER)
        .build();
    trainerRepository.save(trainer);
  }

  private void validatePassword(String rawPassword, String encodedPassword) {
    if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
      throw new WrongPasswordException();
    }
  }

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

    redisTokenRepository.saveAccessToken(username, accessToken, accessTokenExpiryDate);
    redisTokenRepository.saveRefreshToken(username, refreshToken, refreshTokenExpiryDate);

    return new SignInResponseDto(accessToken, refreshToken);
  }

  private void blacklistAndClearCookies(HttpServletRequest request, HttpServletResponse response) {
    Cookie accessTokenCookie = cookieProvider.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
    Cookie refreshTokenCookie = cookieProvider.getCookie(request, REFRESH_TOKEN_COOKIE_NAME);

    blacklistToken(accessTokenCookie);
    blacklistToken(refreshTokenCookie);

    cookieProvider.clearCookie(response, ACCESS_TOKEN_COOKIE_NAME);
    cookieProvider.clearCookie(response, REFRESH_TOKEN_COOKIE_NAME);
  }

  private void blacklistToken(Cookie tokenCookie) {
    if (tokenCookie != null && tokenProvider.validateToken(tokenCookie.getValue())) {
      log.info("블랙리스트에 추가된 토큰: {}", tokenCookie.getValue());
      tokenProvider.blacklistToken(tokenCookie.getValue());
      redisTokenRepository.deleteToken(tokenProvider.getUsernameFromToken(tokenCookie.getValue()));
    }
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return traineeRepository.findByEmail(username)
        .map(UserPrincipal::create)
        .orElseGet(() -> UserPrincipal.create(trainerRepository.findByEmail(username)
            .orElseThrow(UserNotFoundException::new)));
  }

  public MemberInfoResponseDto memberInfo(Long id) {
    return traineeRepository.findById(id)
        .map(trainee -> MemberInfoResponseDto.builder()
            .id(trainee.getId())
            .email(trainee.getEmail())
            .name(trainee.getName())
            .role(trainee.getRole())
            .build())
        .orElseGet(() -> trainerRepository.findById(id)
            .map(trainer -> MemberInfoResponseDto.builder()
                .id(trainer.getId())
                .email(trainer.getEmail())
                .name(trainer.getName())
                .role(trainer.getRole())
                .build())
            .orElseThrow(UserNotFoundException::new));
  }
}