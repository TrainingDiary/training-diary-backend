package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.response.notification.NotificationResponseDto;
import com.project.trainingdiary.entity.NotificationEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.NotificationRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@DisplayName("알림 서비스")
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private TraineeRepository traineeRepository;

  @InjectMocks
  private NotificationService notificationService;

  private TrainerEntity trainer;
  private TraineeEntity trainee;

  @BeforeEach
  public void setup() {
    setupTrainee();
    setupTrainer();
  }

  private void setupTrainee() {
    trainee = TraineeEntity.builder()
        .id(10L)
        .email("trainee@example.com")
        .name("김트레이니")
        .role(UserRoleType.TRAINEE)
        .unreadNotification(true)
        .build();
  }

  private void setupTrainer() {
    trainer = TrainerEntity.builder()
        .id(1L)
        .email("trainer@example.com")
        .name("이트레이너")
        .role(UserRoleType.TRAINER)
        .unreadNotification(true)
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

    lenient().when(traineeRepository.findByEmail(trainee.getEmail()))
        .thenReturn(Optional.of(trainee));
  }

  @AfterEach
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("트레이니의 알림 목록 조회 - 성공")
  void getNotificationListTrainee() {
    //given
    setupTraineeAuth();
    PageRequest pageable = PageRequest.of(0, 20);
    List<NotificationEntity> notifications = List.of(
        NotificationEntity.builder()
            .id(100L)
            .body("일정 예약을 수락했습니다.")
            .toTrainee(true)
            .toTrainer(false)
            .trainer(trainer)
            .trainee(trainee)
            .eventDate(LocalDate.of(2024, 10, 10))
            .build()
    );

    Page<NotificationEntity> response = new PageImpl<>(notifications, pageable, 1);

    //when
    when(notificationRepository.findByTrainee_Id(trainee.getId(), pageable))
        .thenReturn(response);
    ArgumentCaptor<TraineeEntity> captor = ArgumentCaptor.forClass(TraineeEntity.class);
    Page<NotificationResponseDto> notificationList = notificationService.getNotificationList(
        pageable);

    //then
    verify(traineeRepository).save(captor.capture());
    assertFalse(captor.getValue().isUnreadNotification());
    assertEquals(
        "일정 예약을 수락했습니다.",
        notificationList.stream().findFirst().get().getNote()
    );
  }

  @Test
  @DisplayName("트레이너의 알림 목록 조회 - 성공")
  void getNotificationListTrainer() {
    //given
    setupTrainerAuth();
    PageRequest pageable = PageRequest.of(0, 20);
    List<NotificationEntity> notifications = List.of(
        NotificationEntity.builder()
            .id(100L)
            .body("일정 예약을 신청했습니다.")
            .toTrainer(true)
            .toTrainee(false)
            .trainer(trainer)
            .trainee(trainee)
            .eventDate(LocalDate.of(2024, 10, 10))
            .build()
    );

    Page<NotificationEntity> response = new PageImpl<>(notifications, pageable, 1);

    //when
    when(notificationRepository.findByTrainer_Id(trainer.getId(), pageable))
        .thenReturn(response);
    ArgumentCaptor<TrainerEntity> captor = ArgumentCaptor.forClass(TrainerEntity.class);
    Page<NotificationResponseDto> notificationList = notificationService.getNotificationList(
        pageable);

    //then
    verify(trainerRepository).save(captor.capture());
    assertFalse(captor.getValue().isUnreadNotification());
    assertEquals(
        "일정 예약을 신청했습니다.",
        notificationList.stream().findFirst().get().getNote()
    );
  }
}