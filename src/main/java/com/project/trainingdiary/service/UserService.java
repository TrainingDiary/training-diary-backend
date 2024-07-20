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
import com.project.trainingdiary.exception.impl.TrainerNotFoundException;
import com.project.trainingdiary.exception.impl.UserEmailDuplicateException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.exception.impl.VerificationCodeExpiredException;
import com.project.trainingdiary.exception.impl.VerificationCodeNotMatchedException;
import com.project.trainingdiary.exception.impl.VerificationCodeNotYetVerifiedException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
   * <p>
   * 이 메서드는 제공된 이메일로 인증 엔티티를 조회하고, 비밀번호 일치 여부를 확인하며, 인증이 완료되었는지를 검증합니다. 검증에 성공하면 사용자를 저장하고, 토큰을 생성하여
   * 쿠키에 설정한 후, 인증 엔티티를 삭제합니다.
   *
   * @param dto      회원가입 요청 DTO
   * @param response HTTP 응답 객체
   */
  @Transactional
  public void signUp(SignUpRequestDto dto, HttpServletResponse response) {
    VerificationEntity verificationEntity = getVerificationEntity(dto.getEmail());
    validateEmailNotExists(dto.getEmail());
    validatePasswordsMatch(dto.getPassword(), dto.getConfirmPassword());
    validateIfVerified(verificationEntity);

    String encodedPassword = passwordEncoder.encode(dto.getPassword());
    saveUser(dto, encodedPassword);
    generateTokensAndSetCookies(dto.getEmail(), response);
    verificationRepository.deleteByEmail(dto.getEmail());
  }

  /**
   * 로그인 요청을 처리합니다.
   * <p>
   * 이 메서드는 제공된 이메일로 사용자 정보를 로드하고, 비밀번호를 검증합니다. 검증에 성공하면 토큰을 생성하여 쿠키에 설정하고, 로그인 응답 DTO를 반환합니다.
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
   * <p>
   * 이 메서드는 요청에 포함된 쿠키에서 토큰을 추출하여 블랙리스트에 추가하고, 응답에서 쿠키를 삭제합니다.
   *
   * @param request  HTTP 요청 객체
   * @param response HTTP 응답 객체
   */
  public void signOut(HttpServletRequest request, HttpServletResponse response) {
    blacklistAndClearCookies(request, response);
  }

  /**
   * 이메일 중복을 확인합니다.
   * <p>
   * 이 메서드는 제공된 이메일이 트레이니 또는 트레이너로 존재하는지를 확인하고, 중복된 경우 적절한 예외를 발생시킵니다.
   *
   * @param email 확인할 이메일
   * @throws UserEmailDuplicateException 유저 이메일이 중복되면 예외 발생
   */
  private void validateEmailNotExists(String email) {
    if (traineeRepository.findByEmail(email).isPresent() || trainerRepository.findByEmail(email)
        .isPresent()) {
      throw new UserEmailDuplicateException();
    }
  }

  /**
   * 인증 코드를 전송합니다.
   * <p>
   * 이 메서드는 인증 코드를 생성하여 제공된 이메일로 전송하고, 인증 엔티티를 저장합니다.
   *
   * @param email 인증 코드를 전송할 이메일
   */
  private void sendVerificationCode(String email) {
    String verificationCode = VerificationCodeGeneratorUtil.generateVerificationCode();
    String expirationTime = VerificationCodeGeneratorUtil.generateExpirationTime();

    emailProvider.sendVerificationEmail(email, verificationCode, expirationTime);
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
   * 제공된 인증 코드를 검증합니다.
   * <p>
   * 이 메서드는 제공된 인증 코드가 저장된 인증 엔티티의 코드와 일치하는지, 그리고 코드가 만료되지 않았는지를 확인합니다. 두 조건 중 하나라도 실패하면 적절한 예외가
   * 발생합니다. 검증에 성공하면 엔티티는 검증 상태를 반영하도록 업데이트되며, 만료 시간은 무효화됩니다.
   *
   * @param verificationEntity 저장된 코드와 만료 시간이 포함된 인증 엔티티
   * @param verificationCode   제공된 인증 코드
   * @throws VerificationCodeNotMatchedException 인증 코드가 일치하지 않으면 예외 발생
   * @throws VerificationCodeExpiredException    인증 코드가 만료되면 예외 발생
   */
  private void validateVerificationCode(VerificationEntity verificationEntity,
      String verificationCode) {
    if (!verificationEntity.getVerificationCode().equals(verificationCode)) {
      throw new VerificationCodeNotMatchedException();
    }
    if (!verificationEntity.isVerified() &&
        verificationEntity.getExpiredAt() != null &&
        verificationEntity.getExpiredAt().isBefore(LocalDateTime.now())) {
      throw new VerificationCodeExpiredException();
    }
    verificationEntity.setExpiredAt(null); // 무효화
    verificationEntity.setVerified(true);
    verificationRepository.save(verificationEntity);
  }

  /**
   * 인증이 완료되었는지 확인합니다.
   * <p>
   * 이 메서드는 인증 엔티티가 검증되었는지를 확인하고, 검증되지 않았으면 예외를 발생시킵니다.
   *
   * @param verificationEntity 인증 엔티티
   * @throws VerificationCodeNotYetVerifiedException 인증이 완료되지 않았으면 예외 발생
   */
  private void validateIfVerified(VerificationEntity verificationEntity) {
    if (!verificationEntity.isVerified()) {
      throw new VerificationCodeNotYetVerifiedException();
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

    return new SignInResponseDto(username,
        loadUserByUsername(username).getAuthorities().toString());
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
   * <p>
   * //   * @param id 회원 ID
   *
   * @return MemberInfoResponseDto 회원 정보 응답 DTO
   * @throws UserNotFoundException 회원이 존재하지 않으면 예외 발생
   */
  public MemberInfoResponseDto memberInfo() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getName() == null) {
      throw new UserNotFoundException();
    }
    String role = authentication.getAuthorities().toString();

    if (role.contains("ROLE_TRAINER")) {
      return trainerRepository.findByEmail(authentication.getName())
          .map(trainer -> MemberInfoResponseDto.builder()
              .id(trainer.getId())
              .email(trainer.getEmail())
              .name(trainer.getName())
              .role(trainer.getRole())
              .build())
          .orElseThrow(TrainerNotFoundException::new);
    }
    return traineeRepository.findByEmail(authentication.getName())
        .map(trainee -> MemberInfoResponseDto.builder()
            .id(trainee.getId())
            .email(trainee.getEmail())
            .name(trainee.getName())
            .role(trainee.getRole())
            .build())
        .orElseThrow(TrainerNotFoundException::new);
  }
}