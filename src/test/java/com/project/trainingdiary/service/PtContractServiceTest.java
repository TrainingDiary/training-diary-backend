package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.CreatePtContractRequestDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.PtContractAlreadyExistException;
import com.project.trainingdiary.exception.impl.PtContractNotExistException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.UserRoleType;
import com.project.trainingdiary.repository.PtContractRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
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

  @AfterEach
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("PT 계약 생성 - 성공")
  void createPtContract() {
    //given
    CreatePtContractRequestDto dto = new CreatePtContractRequestDto();
    dto.setTraineeEmail(trainee.getEmail());
    dto.setSessionCount(0);

    //when
    when(traineeRepository.findByEmail(trainee.getEmail()))
        .thenReturn(Optional.of(trainee));
    when(ptContractRepository.existsByTrainerIdAndTraineeId(trainer.getId(), trainee.getId()))
        .thenReturn(false);

    //then
    ptContractService.createPtContract(dto);
  }

  @Test
  @DisplayName("PT 계약 생성 - 실패(트레이니를 찾을 수 없는 경우)")
  void createPtContractFail_TraineeNotFoundFromEmail() {
    //given
    CreatePtContractRequestDto dto = new CreatePtContractRequestDto();
    dto.setTraineeEmail(trainee.getEmail());
    dto.setSessionCount(0);

    //when
    when(traineeRepository.findByEmail(trainee.getEmail()))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        UserNotFoundException.class,
        () -> ptContractService.createPtContract(dto)
    );
  }

  @Test
  @DisplayName("PT 계약 생성 - 실패(이미 계약이 존재하는 경우)")
  void createPtContractFail_PtContractAlreadyExist() {
    //given
    CreatePtContractRequestDto dto = new CreatePtContractRequestDto();
    dto.setTraineeEmail(trainee.getEmail());
    dto.setSessionCount(0);

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
  @DisplayName("PT 계약 단건 조회 - 성공")
  void getPtContract() {
    //given
    //when
    when(ptContractRepository.findById(1L))
        .thenReturn(Optional.of(
            PtContractEntity.builder()
                .id(1L)
                .trainee(trainee)
                .trainer(trainer)
                .totalSession(0)
                .totalSessionUpdatedAt(LocalDateTime.now())
                .build()
        ));

    //then
    ptContractService.getPtContract(1L);
  }

  @Test
  @DisplayName("PT 계약 단건 조회 - 실패(없는 계약 id로 조회)")
  void getPtContractFail_NotFound() {
    //given
    //when
    when(ptContractRepository.findById(1L))
        .thenReturn(Optional.empty());

    //then
    assertThrows(
        PtContractNotExistException.class,
        () -> ptContractService.getPtContract(1L)
    );
  }

  @Test
  @DisplayName("PT 계약 목록 조회 - 성공(트레이너)")
  void getPtContractListSuccess_Trainer() {
    //given
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
    when(ptContractRepository.findByTrainer_Email("trainer@example.com", pageRequest))
        .thenReturn(new PageImpl<>(list, pageRequest, 1));

    //then
    ptContractService.getPtContractList(pageRequest);
  }

  @Test
  @DisplayName("PT 계약 목록 조회 - 성공(트레이니)")
  void getPtContractListSuccess_Trainee() {
    //given
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
    when(ptContractRepository.findByTrainee_Email("trainee@example.com", pageRequest))
        .thenReturn(new PageImpl<>(list, pageRequest, 1));

    //then
    ptContractService.getPtContractList(pageRequest);
  }
}