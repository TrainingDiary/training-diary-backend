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
import com.project.trainingdiary.provider.EmailProvider;
import com.project.trainingdiary.provider.TokenProvider;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.VerificationRepository;
import com.project.trainingdiary.util.VerificationCodeGeneratorUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;
  private final VerificationRepository verificationRepository;

  private final EmailProvider emailProvider;
  private final TokenProvider tokenProvider;

  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void checkDuplicateEmailAndSendVerification(
      SendVerificationAndCheckDuplicateRequestDto dto) {

    validateEmailNotExists(dto.getEmail());

    String verificationCode = VerificationCodeGeneratorUtil.generateVerificationCode();
    emailProvider.sendVerificationEmail(dto.getEmail(), verificationCode);

    VerificationEntity verificationEntity = VerificationEntity.of(dto.getEmail(), verificationCode);
    verificationRepository.save(verificationEntity);
  }

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

  @Transactional
  public void signUp(SignUpRequestDto dto) {

    validateEmailNotExists(dto.getEmail());
    validatePasswordsMatch(dto.getPassword(), dto.getConfirmPassword());

    String encodedPassword = passwordEncoder.encode(dto.getPassword());

    if (dto.getRole() == UserRoleType.TRAINEE) {
      saveTrainee(dto, encodedPassword);
    } else if (dto.getRole() == UserRoleType.TRAINER) {
      saveTrainer(dto, encodedPassword);
    } else {
      throw new IllegalArgumentException("Invalid role: " + dto.getRole());
    }

    verificationRepository.deleteByEmail(dto.getEmail());
  }

  public SignInResponseDto signIn(SignInRequestDto dto) {

    UserDetails userDetails = loadUserByUsername(dto.getEmail());

    if (!passwordEncoder.matches(dto.getPassword(), userDetails.getPassword())) {
      throw new WrongPasswordException();
    }

    String token = tokenProvider.createToken(userDetails.getUsername());

    return new SignInResponseDto(token, userDetails.getUsername());
  }

  private void validateEmailNotExists(String email) {
    traineeRepository.findByEmail(email)
        .ifPresent(user -> {
          throw new TraineeEmailDuplicateException();
        });
    trainerRepository.findByEmail(email)
        .ifPresent(user -> {
          throw new TrainerEmailDuplicateException();
        });
  }

  private void validatePasswordsMatch(String password, String confirmPassword) {
    if (!password.equals(confirmPassword)) {
      throw new PasswordMismatchedException();
    }
  }

  private void saveTrainee(SignUpRequestDto dto, String encodedPassword) {
    TraineeEntity trainee = new TraineeEntity();
    trainee.setName(dto.getName());
    trainee.setEmail(dto.getEmail());
    trainee.setPassword(encodedPassword);
    trainee.setRole(UserRoleType.TRAINEE);
    traineeRepository.save(trainee);
  }

  private void saveTrainer(SignUpRequestDto dto, String encodedPassword) {
    TrainerEntity trainer = new TrainerEntity();
    trainer.setName(dto.getName());
    trainer.setEmail(dto.getEmail());
    trainer.setPassword(encodedPassword);
    trainer.setRole(UserRoleType.TRAINER);
    trainerRepository.save(trainer);
  }

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    TraineeEntity trainee = traineeRepository.findByEmail(username).orElseThrow(
        UserNotFoundException::new);

    if (trainee != null) {
      return UserPrincipal.create(trainee);
    }

    TrainerEntity trainer = trainerRepository.findByEmail(username).orElseThrow(
        UserNotFoundException::new);
    return UserPrincipal.create(trainer);
  }
}