package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.component.FcmPushNotification;
import com.project.trainingdiary.dto.request.ptcontract.AddPtContractSessionRequestDto;
import com.project.trainingdiary.dto.request.ptcontract.CreatePtContractRequestDto;
import com.project.trainingdiary.dto.request.ptcontract.TerminatePtContractRequestDto;
import com.project.trainingdiary.entity.NotificationEntity;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.ptcontract.PtContractAlreadyExistException;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.ptcontract.PtContractTraineeCanHaveOnlyOneException;
import com.project.trainingdiary.exception.ptcontract.PtContractTrainerEmailNotExistException;
import com.project.trainingdiary.model.PtContractSort;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.type.NotificationType;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.NotificationRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@DisplayName("PT 계약 서비스")
@ExtendWith(MockitoExtension.class)
class PtContractServiceTest {

  @Mock
  private PtContractRepository ptContractRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private TraineeRepository traineeRepository;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private FcmPushNotification fcmPushNotification;

  @InjectMocks
  private PtContractService ptContractService;

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
  @DisplayName("PT 계약 생성 - 성공")
  void createPtContract() {
    //given
    setupTrainerAuth();
    CreatePtContractRequestDto dto = new CreatePtContractRequestDto();
    dto.setTraineeEmail(trainee.getEmail());

    //when
    when(traineeRepository.findByEmail(trainee.getEmail()))
        .thenReturn(Optional.of(trainee));
    when(ptContractRepository.existsByTrainerIdAndTraineeId(trainer.getId(), trainee.getId()))
        .thenReturn(false);

    ArgumentCaptor<PtContractEntity> captorPtContract = ArgumentCaptor.forClass(
        PtContractEntity.class);
    ArgumentCaptor<NotificationEntity> captorNotification = ArgumentCaptor.forClass(
        NotificationEntity.class);
    ptContractService.createPtContract(dto);

    //then
    verify(ptContractRepository).save(captorPtContract.capture());
    verify(notificationRepository).save(captorNotification.capture());
    assertEquals(NotificationType.PT_CONTRACT_CREATED,
        captorNotification.getValue().getNotificationType());
    assertFalse(captorNotification.getValue().isToTrainer());
    assertTrue(captorNotification.getValue().isToTrainee());
  }

  @Test
  @DisplayName("PT 계약 생성 - 실패(트레이니를 찾을 수 없는 경우)")
  void createPtContractFail_TraineeNotFoundFromEmail() {
    //given
    setupTrainerAuth();
    CreatePtContractRequestDto dto = new CreatePtContractRequestDto();
    dto.setTraineeEmail(trainee.getEmail());

    //when
    when(traineeRepository.findByEmail(trainee.getEmail()))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        PtContractTrainerEmailNotExistException.class,
        () -> ptContractService.createPtContract(dto)
    );
  }

  @Test
  @DisplayName("PT 계약 생성 - 실패(트레이너와 트레이니 사이에 이미 계약이 존재하는 경우)")
  void createPtContractFail_PtContractAlreadyExist() {
    //given
    setupTrainerAuth();
    CreatePtContractRequestDto dto = new CreatePtContractRequestDto();
    dto.setTraineeEmail(trainee.getEmail());

    //when
    when(traineeRepository.findByEmail(trainee.getEmail()))
        .thenReturn(Optional.of(trainee));
    when(ptContractRepository.existsByTrainerIdAndTraineeId(trainer.getId(), trainee.getId()))
        .thenReturn(true);

    //then
    assertThrows(
        PtContractAlreadyExistException.class,
        () -> ptContractService.createPtContract(dto)
    );
  }

  @Test
  @DisplayName("PT 계약 생성 - 실패(트레이니가 다른 트레이너와 이미 계약이 존재하는 경우)")
  void createPtContractFail_PtContractTraineeHaveOnlyOne() {
    //given
    setupTrainerAuth();
    CreatePtContractRequestDto dto = new CreatePtContractRequestDto();
    dto.setTraineeEmail(trainee.getEmail());

    //when
    when(traineeRepository.findByEmail(trainee.getEmail()))
        .thenReturn(Optional.of(trainee));
    when(ptContractRepository.existsByTrainerIdAndTraineeId(trainer.getId(), trainee.getId()))
        .thenReturn(false);
    when(ptContractRepository.existsByTraineeId(trainee.getId()))
        .thenReturn(true);

    //then
    assertThrows(
        PtContractTraineeCanHaveOnlyOneException.class,
        () -> ptContractService.createPtContract(dto)
    );
  }

  @Test
  @DisplayName("PT 계약 목록 조회 - 성공(트레이너)")
  void getPtContractListSuccess_Trainer() {
    //given
    setupTrainerAuth();
    List<PtContractEntity> list = List.of(
        PtContractEntity.builder()
            .id(1L)
            .trainee(trainee)
            .trainer(trainer)
            .totalSession(0)
            .totalSessionUpdatedAt(LocalDateTime.now())
            .build()
    );
    Pageable pageRequest = PageRequest.of(0, 20);

    //when
    when(ptContractRepository.findByTrainerEmail("trainer@example.com", pageRequest,
        PtContractSort.NAME))
        .thenReturn(new PageImpl<>(list, pageRequest, 1));

    //then
    ptContractService.getPtContractList(pageRequest, PtContractSort.NAME);
  }

  @Test
  @DisplayName("PT 계약 목록 조회 - 성공(트레이니)")
  void getPtContractListSuccess_Trainee() {
    //given
    setupTraineeAuth();
    List<PtContractEntity> list = List.of(
        PtContractEntity.builder()
            .id(1L)
            .trainee(trainee)
            .trainer(trainer)
            .totalSession(0)
            .totalSessionUpdatedAt(LocalDateTime.now())
            .build()
    );
    Pageable pageRequest = PageRequest.of(0, 20);

    //when
    when(ptContractRepository.findByTraineeEmail("trainee@example.com", pageRequest,
        PtContractSort.NAME))
        .thenReturn(new PageImpl<>(list, pageRequest, 1));

    //then
    ptContractService.getPtContractList(pageRequest, PtContractSort.NAME);
  }

  @Test
  @DisplayName("PT 계약 횟수 증가 - 성공")
  void increasePtContractSession() {
    //given
    setupTrainerAuth();
    AddPtContractSessionRequestDto dto = new AddPtContractSessionRequestDto();
    dto.setTraineeId(10L);
    dto.setAddition(20);

    //when
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), 10L))
        .thenReturn(Optional.of(
            PtContractEntity.builder()
                .id(1L)
                .trainee(trainee)
                .trainer(trainer)
                .totalSession(0)
                .totalSessionUpdatedAt(LocalDateTime.now())
                .build()
        ));

    ArgumentCaptor<PtContractEntity> captor = ArgumentCaptor.forClass(PtContractEntity.class);
    ptContractService.addPtContractSession(dto);

    //then
    verify(ptContractRepository).save(captor.capture());
    assertEquals(20, captor.getValue().getTotalSession());
  }

  @Test
  @DisplayName("PT 계약 횟수 증가 - 실패(둘 사이 계약이 없는 경우)")
  void increasePtContractSessionFail_NoContract() {
    //given
    setupTrainerAuth();
    AddPtContractSessionRequestDto dto = new AddPtContractSessionRequestDto();
    dto.setTraineeId(10L);
    dto.setAddition(20);

    //when
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), 10L))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        PtContractNotExistException.class,
        () -> ptContractService.addPtContractSession(dto)
    );
  }

  @Test
  @DisplayName("PT 계약 종료 - 성공")
  void terminatePtContract() {
    //given
    setupTrainerAuth();
    TerminatePtContractRequestDto dto = new TerminatePtContractRequestDto();
    dto.setPtContractId(100L);

    //when
    when(ptContractRepository.findByIdAndIsTerminatedFalse(100L))
        .thenReturn(Optional.of(
            PtContractEntity.builder()
                .id(100L)
                .trainee(trainee)
                .trainer(trainer)
                .totalSession(0)
                .totalSessionUpdatedAt(LocalDateTime.now())
                .build()
        ));

    //then
    ptContractService.terminatePtContract(dto);
  }

  @Test
  @DisplayName("PT 계약 종료 - 실패(계약이 존재하지 않거나, 이미 종료된 계약이어서 조회되지 않음)")
  void terminatePtContractFail_NoContract() {
    //given
    setupTrainerAuth();
    TerminatePtContractRequestDto dto = new TerminatePtContractRequestDto();
    dto.setPtContractId(100L);

    //when
    when(ptContractRepository.findByIdAndIsTerminatedFalse(100L))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        PtContractNotExistException.class,
        () -> ptContractService.terminatePtContract(dto)
    );
  }
}