package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import com.project.trainingdiary.dto.request.trainer.AddInBodyInfoRequestDto;
import com.project.trainingdiary.dto.request.trainer.EditTraineeInfoRequestDto;
import com.project.trainingdiary.dto.response.trainer.AddInBodyInfoResponseDto;
import com.project.trainingdiary.dto.response.trainer.EditTraineeInfoResponseDto;
import com.project.trainingdiary.dto.response.trainer.TraineeInfoResponseDto;
import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.user.TraineeNotFoundException;
import com.project.trainingdiary.exception.user.TrainerNotFoundException;
import com.project.trainingdiary.exception.user.UnauthorizedTraineeException;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.type.GenderType;
import com.project.trainingdiary.model.type.TargetType;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.InBodyRecordHistoryRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import java.time.LocalDate;
import java.util.ArrayList;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceTest {

  @Mock
  private TraineeRepository traineeRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private InBodyRecordHistoryRepository inBodyRecordHistoryRepository;

  @Mock
  private PtContractRepository ptContractRepository;

  @Mock
  private Authentication authentication;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Cache<String, UserPrincipal> userCache;

  @InjectMocks
  private TrainerService trainerService;

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
        .build();
  }

  private void setupTrainer() {
    trainer = TrainerEntity.builder()
        .id(1L)
        .email("trainer@example.com")
        .name("이트레이너")
        .role(UserRoleType.TRAINER)
        .build();
  }

  private PtContractEntity createPtContract(TrainerEntity trainer, TraineeEntity trainee) {
    PtContractEntity contract = new PtContractEntity();
    contract.setTrainer(trainer);
    contract.setTrainee(trainee);
    contract.setTotalSession(10);
    contract.setUsedSession(5);
    return contract;
  }

  private List<InBodyRecordHistoryEntity> createInBodyRecords(TraineeEntity trainee) {
    List<InBodyRecordHistoryEntity> inBodyRecords = new ArrayList<>();
    InBodyRecordHistoryEntity inBodyRecordHistory = new InBodyRecordHistoryEntity();
    inBodyRecordHistory.setTrainee(trainee);
    inBodyRecordHistory.setWeight(10);
    inBodyRecordHistory.setSkeletalMuscleMass(5);
    inBodyRecordHistory.setBodyFatPercentage(80.0);
    inBodyRecords.add(inBodyRecordHistory);
    return inBodyRecords;
  }

  private void setupTrainerAuth() {
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
  public void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("인증되지 않은 유저가 트레이니 정보를 요청할 때 예외 발생")
  void testGetTraineeInfo_AuthenticationFailure() {
    // given
    lenient().when(securityContext.getAuthentication()).thenReturn(null);
    // when / then
    assertThrows(UserNotFoundException.class, () -> trainerService.getTraineeInfo(1L));
  }

  @Test
  @DisplayName("인증되지 않은 트레이너가 인바디 기록을 추가할 때 예외 발생")
  void testAddInBodyRecord_AuthenticationFailure() {
    // given
    AddInBodyInfoRequestDto dto = new AddInBodyInfoRequestDto();
    dto.setTraineeId(1L);

    lenient().when(securityContext.getAuthentication()).thenReturn(null);

    // when / then
    assertThrows(TrainerNotFoundException.class, () -> trainerService.addInBodyRecord(dto));
  }

  @Test
  @DisplayName("인증되지 않은 트레이너가 트레이니 정보를 수정할 때 예외 발생")
  void testEditTraineeInfo_AuthenticationFailure() {
    // given
    EditTraineeInfoRequestDto dto = new EditTraineeInfoRequestDto();
    dto.setTraineeId(1L);

    lenient().when(securityContext.getAuthentication()).thenReturn(null);

    // when / then
    assertThrows(TrainerNotFoundException.class, () -> trainerService.editTraineeInfo(dto));
  }

  @Test
  @DisplayName("트레이너 트레이니 정보 조회 성공")
  void testTrainerGetTraineeInfo_Success() {
    // given
    setupTrainer();
    setupTrainee();
    setupTrainerAuth();

    PtContractEntity contract = createPtContract(trainer, trainee);
    List<InBodyRecordHistoryEntity> inBodyRecords = createInBodyRecords(trainee);
    trainee.setInBodyRecords(inBodyRecords);

    when(trainerRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
    when(ptContractRepository.findWithTraineeAndTrainer(trainee.getId(),
        trainer.getId())).thenReturn(Optional.of(contract));

    // when
    TraineeInfoResponseDto responseDto = trainerService.getTraineeInfo(trainee.getId());

    // then
    assertEquals(5, responseDto.getRemainingSession());
    assertEquals(trainee.getId(), responseDto.getTraineeId());
  }

  @Test
  @DisplayName("트레이니가 자신의 정보를 조회할 때 성공")
  void testTraineeViewingOwnInfo_Success() {
    // given
    setupTrainee();
    setupTraineeAuth();

    PtContractEntity contract = createPtContract(trainer, trainee);
    List<InBodyRecordHistoryEntity> inBodyRecords = createInBodyRecords(trainee);
    trainee.setInBodyRecords(inBodyRecords);

    when(traineeRepository.findByEmail(trainee.getEmail())).thenReturn(Optional.of(trainee));
    when(ptContractRepository.findByTraineeIdWithInBodyRecords(trainee.getId())).thenReturn(
        Optional.of(contract));

    // when
    TraineeInfoResponseDto responseDto = trainerService.getTraineeInfo(trainee.getId());

    // then
    assertEquals(5, responseDto.getRemainingSession());
    assertEquals(trainee.getId(), responseDto.getTraineeId());
  }

  @Test
  @DisplayName("트레이니가 다른 트레이니의 정보를 조회할 때 실패")
  void testTraineeViewingOtherTraineeInfo_Fail() {
    // given
    setupTrainee();
    setupTraineeAuth();

    TraineeEntity otherTrainee = TraineeEntity.builder()
        .id(20L)
        .email("othertrainee@example.com")
        .name("다른트레이니")
        .role(UserRoleType.TRAINEE)
        .build();

    List<InBodyRecordHistoryEntity> inBodyRecords = createInBodyRecords(otherTrainee);
    otherTrainee.setInBodyRecords(inBodyRecords);

    when(traineeRepository.findByEmail(trainee.getEmail())).thenReturn(Optional.of(trainee));
    // when / then
    assertThrows(UnauthorizedTraineeException.class,
        () -> trainerService.getTraineeInfo(otherTrainee.getId()));
  }

  @Test
  @DisplayName("트레이니가 존재하지 않을 때 예외 발생")
  void testGetTraineeInfo_TraineeNotExistException() {
    // given
    setupTrainer();
    setupTrainerAuth();

    Long traineeId = 1L;
    when(ptContractRepository.findWithTraineeAndTrainer(traineeId, trainer.getId())).thenReturn(
        Optional.empty());

    // when / then
    assertThrows(PtContractNotExistException.class, () -> trainerService.getTraineeInfo(traineeId));
  }

  @Test
  @DisplayName("트레이니 정보 수정 성공")
  void testEditTraineeInfo_Success() {
    // given
    setupTrainer();
    setupTrainerAuth();

    Long traineeId = 1L;
    Long trainerId = trainer.getId();

    TraineeEntity trainee = new TraineeEntity();
    trainee.setId(traineeId);

    EditTraineeInfoRequestDto dto = new EditTraineeInfoRequestDto();
    dto.setTraineeId(traineeId);
    dto.setBirthDate(LocalDate.parse("2000-01-01"));
    dto.setGender(GenderType.MALE);
    dto.setHeight(180);
    dto.setTargetType(TargetType.TARGET_BODY_FAT_PERCENTAGE);
    dto.setTargetValue(70);
    dto.setTargetReward("Reward");
    dto.setRemainingSession(20);

    when(trainerRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainerId, traineeId)).thenReturn(
        Optional.of(
            PtContractEntity.builder()
                .trainer(trainer)
                .trainee(trainee)
                .totalSession(10)
                .build()
        )
    );

    // when
    EditTraineeInfoResponseDto responseDto = trainerService.editTraineeInfo(dto);

    // then
    assertEquals(dto.getBirthDate(), responseDto.getBirthDate());
    assertEquals(dto.getGender(), responseDto.getGender());
    assertEquals(dto.getHeight(), responseDto.getHeight());
    assertEquals(dto.getTargetType(), responseDto.getTargetType());
    assertEquals(dto.getTargetValue(), responseDto.getTargetValue());
    assertEquals(dto.getTargetReward(), responseDto.getTargetReward());
    assertEquals(dto.getRemainingSession(), responseDto.getRemainingSession());

    verify(trainerRepository, times(1)).findByEmail(trainer.getEmail());
    verify(traineeRepository, times(1)).findById(traineeId);
    verify(ptContractRepository, times(1)).findByTrainerIdAndTraineeId(trainerId, traineeId);
  }

  @Test
  @DisplayName("트레이니 정보 수정 시 트레이니가 존재하지 않을 때 예외 발생")
  void testEditTraineeInfo_TraineeNotExistException() {
    // given
    setupTrainer();
    setupTrainerAuth();

    Long traineeId = 1L;
    String trainerEmail = trainer.getEmail();

    EditTraineeInfoRequestDto dto = new EditTraineeInfoRequestDto();
    dto.setTraineeId(traineeId);

    when(trainerRepository.findByEmail(trainerEmail)).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.empty());

    // when / then
    assertThrows(TraineeNotFoundException.class, () -> trainerService.editTraineeInfo(dto));
    verify(trainerRepository, times(1)).findByEmail(trainerEmail);
    verify(traineeRepository, times(1)).findById(traineeId);
  }

  @Test
  @DisplayName("인바디 기록 추가 성공")
  void testAddInBodyRecord_Success() {
    // given
    setupTrainer();
    setupTrainee();
    setupTrainerAuth();

    Long traineeId = trainee.getId();
    Long trainerId = trainer.getId();

    AddInBodyInfoRequestDto dto = new AddInBodyInfoRequestDto();
    dto.setTraineeId(traineeId);
    dto.setWeight(70.0);
    dto.setSkeletalMuscleMass(30.0);
    dto.setBodyFatPercentage(20.0);

    InBodyRecordHistoryEntity inBodyRecord = new InBodyRecordHistoryEntity();
    inBodyRecord.setTrainee(trainee);
    inBodyRecord.setWeight(dto.getWeight());
    inBodyRecord.setSkeletalMuscleMass(dto.getSkeletalMuscleMass());
    inBodyRecord.setBodyFatPercentage(dto.getBodyFatPercentage());

    when(trainerRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
    when(ptContractRepository.existsByTrainerIdAndTraineeId(trainerId, traineeId)).thenReturn(true);
    when(inBodyRecordHistoryRepository.save(any(InBodyRecordHistoryEntity.class))).thenReturn(
        inBodyRecord);

    // when
    AddInBodyInfoResponseDto responseDto = trainerService.addInBodyRecord(dto);

    // then
    assertEquals(dto.getWeight(), responseDto.getWeight());
    assertEquals(dto.getSkeletalMuscleMass(), responseDto.getSkeletalMuscleMass());
    assertEquals(dto.getBodyFatPercentage(), responseDto.getBodyFatPercentage());

    verify(trainerRepository, times(1)).findByEmail(trainer.getEmail());
    verify(traineeRepository, times(1)).findById(traineeId);
    verify(ptContractRepository, times(1)).existsByTrainerIdAndTraineeId(trainerId, traineeId);
    verify(inBodyRecordHistoryRepository, times(1)).save(any(InBodyRecordHistoryEntity.class));
  }

  @Test
  @DisplayName("인바디 기록 추가 시 트레이니가 존재하지 않을 때 예외 발생")
  void testAddInBodyRecord_TraineeNotExistException() {
    // given
    setupTrainerAuth();
    Long traineeId = 1L;

    AddInBodyInfoRequestDto dto = new AddInBodyInfoRequestDto();
    dto.setTraineeId(traineeId);

    when(trainerRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.empty());

    // when / then
    assertThrows(TraineeNotFoundException.class, () -> trainerService.addInBodyRecord(dto));
    verify(trainerRepository, times(1)).findByEmail(trainer.getEmail());
    verify(traineeRepository, times(1)).findById(traineeId);

  }

  @Test
  @DisplayName("트레이너와 트레이니 사이에 계약이 존재하지 않을 때 예외 발생")
  void testGetTraineeInfo_ContractNotExist() {
    // given
    setupTrainerAuth();
    Long traineeId = 1L;

    when(ptContractRepository.findWithTraineeAndTrainer(traineeId, trainer.getId())).thenReturn(
        Optional.empty());

    // when / then
    assertThrows(PtContractNotExistException.class, () -> trainerService.getTraineeInfo(traineeId));
  }

  @Test
  @DisplayName("트레이너와 트레이니 사이에 계약이 존재하지 않을 때 인바디 기록 추가 시 예외 발생")
  void testAddInBodyRecord_ContractNotExist() {
    // given
    setupTrainerAuth();
    Long traineeId = 1L;

    TraineeEntity trainee = new TraineeEntity();
    trainee.setId(traineeId);

    AddInBodyInfoRequestDto dto = new AddInBodyInfoRequestDto();
    dto.setTraineeId(traineeId);

    when(trainerRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
    when(ptContractRepository.existsByTrainerIdAndTraineeId(trainer.getId(), traineeId)).thenReturn(
        false);

    // when / then
    assertThrows(PtContractNotExistException.class, () -> trainerService.addInBodyRecord(dto));
  }

  @Test
  @DisplayName("트레이너와 트레이니 사이에 계약이 존재하지 않을 때 트레이니 정보 수정 시 예외 발생")
  void testEditTraineeInfo_ContractNotExist() {
    // given
    setupTrainerAuth();

    Long traineeId = 1L;
    String trainerEmail = "trainer@example.com";

    TraineeEntity trainee = new TraineeEntity();
    trainee.setId(traineeId);

    EditTraineeInfoRequestDto dto = new EditTraineeInfoRequestDto();
    dto.setTraineeId(traineeId);

    when(trainerRepository.findByEmail(trainerEmail)).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), traineeId)).thenReturn(
        Optional.empty());

    // when / then
    assertThrows(PtContractNotExistException.class, () -> trainerService.editTraineeInfo(dto));
  }

  @Test
  @DisplayName("캐시된 트레이니가 자신의 정보를 조회할 때 성공")
  void testTraineeGetOwnInfo_CachedSuccess() {
    // given
    setupTrainee();
    setupTraineeAuth();

    PtContractEntity contract = createPtContract(trainer, trainee);
    List<InBodyRecordHistoryEntity> inBodyRecords = createInBodyRecords(trainee);
    trainee.setInBodyRecords(inBodyRecords);

    UserPrincipal cachedUser = UserPrincipal.create(trainee);
    when(userCache.getIfPresent(trainee.getEmail())).thenReturn(cachedUser);
    when(ptContractRepository.findByTraineeIdWithInBodyRecords(trainee.getId())).thenReturn(
        Optional.of(contract));

    // when
    TraineeInfoResponseDto responseDto = trainerService.getTraineeInfo(trainee.getId());

    // then
    assertEquals(5, responseDto.getRemainingSession());
    assertEquals(trainee.getId(), responseDto.getTraineeId());
    verify(userCache, times(1)).getIfPresent(trainee.getEmail());
    verify(ptContractRepository, times(1)).findByTraineeIdWithInBodyRecords(trainee.getId());
  }

  @Test
  @DisplayName("캐시된 트레이너가 트레이니 정보를 조회할 때 성공")
  void testTrainerGetTraineeInfo_CachedSuccess() {
    // given
    setupTrainer();
    setupTrainee();
    setupTrainerAuth();

    PtContractEntity contract = createPtContract(trainer, trainee);
    List<InBodyRecordHistoryEntity> inBodyRecords = createInBodyRecords(trainee);
    trainee.setInBodyRecords(inBodyRecords);

    UserPrincipal cachedUser = UserPrincipal.create(trainer);
    when(userCache.getIfPresent(trainer.getEmail())).thenReturn(cachedUser);
    when(ptContractRepository.findWithTraineeAndTrainer(trainee.getId(),
        trainer.getId())).thenReturn(Optional.of(contract));

    // when
    TraineeInfoResponseDto responseDto = trainerService.getTraineeInfo(trainee.getId());

    // then
    assertEquals(5, responseDto.getRemainingSession());
    assertEquals(trainee.getId(), responseDto.getTraineeId());
    verify(userCache, times(1)).getIfPresent(trainer.getEmail());
    verify(ptContractRepository, times(1)).findWithTraineeAndTrainer(trainee.getId(),
        trainer.getId());
  }

  @Test
  @DisplayName("트레이니 정보 수정 시 계약이 유효하지 않을 때 예외 발생")
  void testEditTraineeInfo_InvalidContract() {
    // given
    setupTrainerAuth();
    Long traineeId = 1L;

    EditTraineeInfoRequestDto dto = new EditTraineeInfoRequestDto();
    dto.setTraineeId(traineeId);

    TraineeEntity trainee = new TraineeEntity();
    trainee.setId(traineeId);

    when(trainerRepository.findByEmail(trainer.getEmail())).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), traineeId)).thenReturn(
        Optional.empty());

    // when / then
    assertThrows(PtContractNotExistException.class, () -> trainerService.editTraineeInfo(dto));
  }

}