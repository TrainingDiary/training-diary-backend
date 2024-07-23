package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.notification.RegisterFcmTokenRequestDto;
import com.project.trainingdiary.entity.FcmTokenEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.FcmTokenRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@DisplayName("FCM 토큰 서비스")
@ExtendWith(MockitoExtension.class)
class FcmTokenServiceTest {

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private TraineeRepository traineeRepository;

  @Mock
  private FcmTokenRepository fcmTokenRepository;

  @InjectMocks
  private FcmTokenService fcmTokenService;

  private TrainerEntity trainer;
  private TraineeEntity trainee;

  @BeforeEach
  public void setup() {
    setupTrainee();
    setupTrainer();
  }

  private void setupTrainee() {
    trainee = new TraineeEntity();
    trainee.setId(10L);
    trainee.setEmail("trainee@example.com");
    trainee.setName("김트레이니");
    trainee.setRole(UserRoleType.TRAINEE);
  }

  private void setupTrainer() {
    trainer = TrainerEntity.builder()
        .id(1L)
        .email("trainer@example.com")
        .name("이트레이너")
        .role(UserRoleType.TRAINER)
        .build();
  }

  private void setupTrainerAuth() {
    // 트레이너의 인증정보가 들어있는 상태
    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_TRAINER");
    Collection authorities = Collections.singleton(authority);

    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getAuthorities()).thenReturn(authorities);

    UserDetails userDetails = UserPrincipal.create(trainer);
    lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
    lenient().when(authentication.getName()).thenReturn(trainer.getEmail());

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    lenient().when(trainerRepository.findByEmail(trainer.getEmail()))
        .thenReturn(Optional.of(trainer));
  }

  private void setupTraineeAuth() {
    // 트레이니의 인증정보가 들어있는 상태
    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_TRAINEE");
    Collection authorities = Collections.singleton(authority);

    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getAuthorities()).thenReturn(authorities);

    UserDetails userDetails = UserPrincipal.create(trainee);
    lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
    lenient().when(authentication.getName()).thenReturn(trainee.getEmail());

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  @AfterEach
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("트레이니의 토큰 저장 - 성공")
  void registerFcmTokenTrainee() {
    //given
    setupTraineeAuth();
    RegisterFcmTokenRequestDto dto = new RegisterFcmTokenRequestDto();
    dto.setToken("token1");

    //when
    when(traineeRepository.findByEmail(trainee.getEmail()))
        .thenReturn(Optional.of(trainee));
    fcmTokenService.registerFcmToken(dto);
    ArgumentCaptor<TraineeEntity> captorTrainee = ArgumentCaptor.forClass(TraineeEntity.class);
    ArgumentCaptor<FcmTokenEntity> captorFcmToken = ArgumentCaptor.forClass(FcmTokenEntity.class);

    //then
    verify(traineeRepository).save(captorTrainee.capture());
    verify(fcmTokenRepository).save(captorFcmToken.capture());
    assertEquals("token1", captorTrainee.getValue().getFcmToken().getToken());
    assertEquals("token1", captorFcmToken.getValue().getToken());
  }

  @Test
  @DisplayName("트레이너의 토큰 저장 - 성공")
  void registerFcmTokenTrainer() {
    //given
    setupTrainerAuth();
    RegisterFcmTokenRequestDto dto = new RegisterFcmTokenRequestDto();
    dto.setToken("token2");

    //when
    when(trainerRepository.findByEmail(trainer.getEmail()))
        .thenReturn(Optional.of(trainer));
    fcmTokenService.registerFcmToken(dto);
    ArgumentCaptor<TrainerEntity> captorTrainer = ArgumentCaptor.forClass(TrainerEntity.class);
    ArgumentCaptor<FcmTokenEntity> captorFcmToken = ArgumentCaptor.forClass(FcmTokenEntity.class);

    //then
    verify(trainerRepository).save(captorTrainer.capture());
    assertEquals("token2", captorTrainer.getValue().getFcmToken().getToken());
    assertEquals("token2", captorFcmToken.getValue().getToken());
  }
}