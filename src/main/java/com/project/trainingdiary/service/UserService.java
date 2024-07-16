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

  /**
   * 이메일 중복을 확인하고 인증 코드를 전송합니다.
   *
   * @param dto 이메일 중복 확인 및 인증 코드 전송 요청 DTO
   */
  public void checkDuplicateEmailAndSendVerification(
      SendVerificationAndCheckDuplicateRequestDto dto) {
    validateEmailNotExists(dto.getEmail());
    sendVerificationCode(dto.getEmail());
  }

  /**
   * 인증 코드를 확인합니다.
   *
   * @param dto 인증 코드 확인 요청 DTO
   */
  public void checkVerificationCode(VerifyCodeRequestDto dto) {
    VerificationEntity verificationEntity = getVerificationEntity(dto.getEmail());
    validateVerificationCode(verificationEntity, dto.getVerificationCode());
  }

  /**
   * 회원가입을 처리합니다.
   *
   * @param dto      회원가입 요청 DTO
   * @param response HTTP 응답 객체
   */
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

  /**
   * 로그인 요청을 처리합니다.
   *
   * @param dto      로그인 요청 DTO
   * @param response HTTP 응답 객체
   * @return SignInResponseDto 로그인 응답 DTO
   */
  public SignInResponseDto signIn(SignInRequestDto dto, HttpServletResponse response) {
    UserDetails userDetails = loadUserByUsername(dto.getEmail());
    validatePassword(dto.getPassword(), userDetails.getPassword());
    return generateTokensAndSetCookies(userDetails.getUsername(), response);
  }

  /**
   * 로그아웃 요청을 처리합니다.
   *
   * @param request  HTTP 요청 객체
   * @param response HTTP 응답 객체
   */
  public void signOut(HttpServletRequest request, HttpServletResponse response) {
    blacklistAndClearCookies(request, response);
  }

  /**
   * 이메일 중복을 확인합니다.
   *
   * @param email 확인할 이메일
   * @throws TraineeEmailDuplicateException 트레이니 이메일이 중복되면 예외 발생
   * @throws TrainerEmailDuplicateException 트레이너 이메일이 중복되면 예외 발생
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
   * 인증 코드를 전송합니다.
   *
   * @param email 인증 코드를 전송할 이메일
   */
  private void sendVerificationCode(String email) {
    String verificationCode = VerificationCodeGeneratorUtil.generateVerificationCode();
    emailProvider.sendVerificationEmail(email, verificationCode);
    verificationRepository.save(VerificationEntity.of(email, verificationCode));
  }

  /**
   * 비밀번호와 비밀번호 확인이 일치하는지 확인합니다.
   *
   * @param password        비밀번호
   * @param confirmPassword 비밀번호 확인
   * @throws PasswordMismatchedException 비밀번호와 비밀번호 확인이 일치하지 않으면 예외 발생
   */
  private void validatePasswordsMatch(String password, String confirmPassword) {
    if (!password.equals(confirmPassword)) {
      throw new PasswordMismatchedException();
    }
  }

  /**
   * 인증 코드를 확인합니다.
   *
   * @param verificationEntity 인증 엔티티
   * @param verificationCode   인증 코드
   * @throws VerificationCodeNotMatchedException 인증 코드가 일치하지 않으면 예외 발생
   * @throws VerificationCodeExpiredException    인증 코드가 만료되면 예외 발생
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
   * 이메일로 인증 엔티티를 조회합니다.
   *
   * @param email 이메일
   * @return VerificationEntity 인증 엔티티
   * @throws UserNotFoundException 인증 엔티티가 존재하지 않으면 예외 발생
   */
  private VerificationEntity getVerificationEntity(String email) {
    return verificationRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
  }

  /**
   * 회원가입 정보를 저장합니다.
   *
   * @param dto             회원가입 요청 DTO
   * @param encodedPassword 인코딩된 비밀번호
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
   * 트레이니 정보를 저장합니다.
   *
   * @param dto             회원가입 요청 DTO
   * @param encodedPassword 인코딩된 비밀번호
   */
  private void saveTrainee(SignUpRequestDto dto, String encodedPassword) {
    TraineeEntity trainee = TraineeEntity.builder()
        .name(dto.getName())
        .email(dto.getEmail())
        .password(encodedPassword)
        .role(UserRoleType.TRAINEE)
        .build();
    traineeRepository.save(trainee);
  }

  /**
   * 트레이너 정보를 저장합니다.
   *
   * @param dto             회원가입 요청 DTO
   * @param encodedPassword 인코딩된 비밀번호
   */
  private void saveTrainer(SignUpRequestDto dto, String encodedPassword) {
    TrainerEntity trainer = TrainerEntity.builder()
        .name(dto.getName())
        .email(dto.getEmail())
        .password(encodedPassword)
        .role(UserRoleType.TRAINER)
        .build();
    trainerRepository.save(trainer);
  }

  /**
   * 비밀번호를 확인합니다.
   *
   * @param rawPassword     입력한 비밀번호
   * @param encodedPassword 저장된 인코딩된 비밀번호
   * @throws WrongPasswordException 비밀번호가 일치하지 않으면 예외 발생
   */
  private void validatePassword(String rawPassword, String encodedPassword) {
    if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
      throw new WrongPasswordException();
    }
  }

  /**
   * 토큰을 생성하고 쿠키에 설정합니다.
   *
   * @param username 사용자 이름
   * @param response HTTP 응답 객체
   * @return SignInResponseDto 로그인 응답 DTO
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

    redisTokenRepository.saveAccessToken(username, accessToken, accessTokenExpiryDate);
    redisTokenRepository.saveRefreshToken(username, refreshToken, refreshTokenExpiryDate);

    return new SignInResponseDto(accessToken, refreshToken);
  }

  /**
   * 블랙리스트에 토큰을 추가하고 쿠키를 삭제합니다.
   *
   * @param request  HTTP 요청 객체
   * @param response HTTP 응답 객체
   */
  private void blacklistAndClearCookies(HttpServletRequest request, HttpServletResponse response) {
    Cookie accessTokenCookie = cookieProvider.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
    Cookie refreshTokenCookie = cookieProvider.getCookie(request, REFRESH_TOKEN_COOKIE_NAME);

    blacklistToken(accessTokenCookie);
    blacklistToken(refreshTokenCookie);

    cookieProvider.clearCookie(response, ACCESS_TOKEN_COOKIE_NAME);
    cookieProvider.clearCookie(response, REFRESH_TOKEN_COOKIE_NAME);
  }

  /**
   * 토큰을 블랙리스트에 추가합니다.
   *
   * @param tokenCookie 토큰 쿠키
   */
  private void blacklistToken(Cookie tokenCookie) {
    if (tokenCookie != null && tokenProvider.validateToken(tokenCookie.getValue())) {
      log.info("블랙리스트에 추가된 토큰: {}", tokenCookie.getValue());
      tokenProvider.blacklistToken(tokenCookie.getValue());
      redisTokenRepository.deleteToken(tokenProvider.getUsernameFromToken(tokenCookie.getValue()));
    }
  }

  /**
   * 사용자 이름으로 사용자 정보를 로드합니다.
   *
   * @param username 사용자 이름
   * @return UserDetails 사용자 정보
   * @throws UsernameNotFoundException 사용자 이름이 존재하지 않으면 예외 발생
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return traineeRepository.findByEmail(username)
        .map(UserPrincipal::create)
        .orElseGet(() -> UserPrincipal.create(trainerRepository.findByEmail(username)
            .orElseThrow(UserNotFoundException::new)));
  }

  /**
   * 회원 정보를 조회합니다.
   *
   * @param id 회원 ID
   * @return MemberInfoResponseDto 회원 정보 응답 DTO
   * @throws UserNotFoundException 회원이 존재하지 않으면 예외 발생
   */
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