package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.AddInBodyInfoRequestDto;
import com.project.trainingdiary.dto.request.EditTraineeInfoRequestDto;
import com.project.trainingdiary.dto.response.AddInBodyInfoResponseDto;
import com.project.trainingdiary.dto.response.EditTraineeInfoResponseDto;
import com.project.trainingdiary.dto.response.TraineeInfoResponseDto;
import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.PtContractNotExistException;
import com.project.trainingdiary.exception.impl.TraineeNotExistException;
import com.project.trainingdiary.exception.impl.TrainerNotFoundException;
import com.project.trainingdiary.model.GenderType;
import com.project.trainingdiary.model.TargetType;
import com.project.trainingdiary.repository.InBodyRecordHistoryRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

  @InjectMocks
  private TrainerService trainerService;

  @BeforeEach
  public void setUp() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
  }

  @Test
  @DisplayName("인증되지 않은 트레이너가 트레이니 정보를 요청할 때 예외 발생")
  void testGetTraineeInfo_AuthenticationFailure() {
    // given
    when(securityContext.getAuthentication()).thenReturn(null);
    // when / then
    assertThrows(TrainerNotFoundException.class, () -> trainerService.getTraineeInfo(1L));
  }

  @Test
  @DisplayName("인증되지 않은 트레이너가 인바디 기록을 추가할 때 예외 발생")
  void testAddInBodyRecord_AuthenticationFailure() {
    // given
    AddInBodyInfoRequestDto dto = new AddInBodyInfoRequestDto();
    dto.setTraineeId(1L);

    when(securityContext.getAuthentication()).thenReturn(null);

    // when / then
    assertThrows(TrainerNotFoundException.class, () -> trainerService.addInBodyRecord(dto));
  }

  @Test
  @DisplayName("인증되지 않은 트레이너가 트레이니 정보를 수정할 때 예외 발생")
  void testEditTraineeInfo_AuthenticationFailure() {
    // given
    EditTraineeInfoRequestDto dto = new EditTraineeInfoRequestDto();
    dto.setTraineeId(1L);

    when(securityContext.getAuthentication()).thenReturn(null);

    // when / then
    assertThrows(TrainerNotFoundException.class, () -> trainerService.editTraineeInfo(dto));
  }

  @Test
  @DisplayName("트레이니 정보 조회 성공")
  void testGetTraineeInfo_Success() {
    // given
    Long traineeId = 1L;
    Long trainerId = 1L;
    String trainerEmail = "trainer@example.com";

    TrainerEntity trainer = new TrainerEntity();
    trainer.setId(trainerId);

    TraineeEntity trainee = new TraineeEntity();
    trainee.setId(traineeId);
    trainee.setInBodyRecords(new ArrayList<>());

    PtContractEntity contract = new PtContractEntity();
    contract.setTrainer(trainer);
    contract.setTrainee(trainee);
    contract.setTotalSession(10);
    contract.setUsedSession(5);

    when(authentication.getName()).thenReturn(trainerEmail);
    when(trainerRepository.findByEmail(trainerEmail)).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
    when(ptContractRepository.existsByTrainerIdAndTraineeId(trainerId, traineeId)).thenReturn(true);
    when(ptContractRepository.findByTrainee(trainee)).thenReturn(List.of(contract));

    // when
    TraineeInfoResponseDto responseDto = trainerService.getTraineeInfo(traineeId);

    // then
    assertEquals(5, responseDto.getRemainingSessions());
    assertEquals(traineeId, responseDto.getTraineeId());
    verify(trainerRepository, times(1)).findByEmail(trainerEmail);
    verify(traineeRepository, times(1)).findById(traineeId);
    verify(ptContractRepository, times(1)).existsByTrainerIdAndTraineeId(trainerId, traineeId);
  }

  @Test
  @DisplayName("트레이니가 존재하지 않을 때 예외 발생")
  void testGetTraineeInfo_TraineeNotExistException() {
    // given
    Long traineeId = 1L;
    String trainerEmail = "trainer@example.com";

    TrainerEntity trainer = new TrainerEntity();
    trainer.setId(1L);

    when(authentication.getName()).thenReturn(trainerEmail);
    when(trainerRepository.findByEmail(trainerEmail)).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.empty());

    // when / then
    assertThrows(TraineeNotExistException.class, () -> trainerService.getTraineeInfo(traineeId));
    verify(trainerRepository, times(1)).findByEmail(trainerEmail);
    verify(traineeRepository, times(1)).findById(traineeId);
  }

  @Test
  @DisplayName("트레이니 정보 수정 성공")
  void testEditTraineeInfo_Success() {
    // given
    Long traineeId = 1L;
    Long trainerId = 1L;
    String trainerEmail = "trainer@example.com";

    TrainerEntity trainer = new TrainerEntity();
    trainer.setId(trainerId);

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

    when(authentication.getName()).thenReturn(trainerEmail);
    when(trainerRepository.findByEmail(trainerEmail)).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
    when(ptContractRepository.existsByTrainerIdAndTraineeId(trainerId, traineeId)).thenReturn(true);

    // when
    EditTraineeInfoResponseDto responseDto = trainerService.editTraineeInfo(dto);

    // then
    assertEquals(dto.getBirthDate(), responseDto.getBirthDate());
    assertEquals(dto.getGender(), responseDto.getGender());
    assertEquals(dto.getHeight(), responseDto.getHeight());
    assertEquals(dto.getTargetType(), responseDto.getTargetType());
    assertEquals(dto.getTargetValue(), responseDto.getTargetValue());
    assertEquals(dto.getTargetReward(), responseDto.getTargetReward());

    verify(trainerRepository, times(1)).findByEmail(trainerEmail);
    verify(traineeRepository, times(1)).findById(traineeId);
    verify(ptContractRepository, times(1)).existsByTrainerIdAndTraineeId(trainerId, traineeId);
  }

  @Test
  @DisplayName("트레이니 정보 수정 시 트레이니가 존재하지 않을 때 예외 발생")
  void testEditTraineeInfo_TraineeNotExistException() {
    // given
    Long traineeId = 1L;
    String trainerEmail = "trainer@example.com";

    TrainerEntity trainer = new TrainerEntity();
    trainer.setId(1L);

    EditTraineeInfoRequestDto dto = new EditTraineeInfoRequestDto();
    dto.setTraineeId(traineeId);

    when(authentication.getName()).thenReturn(trainerEmail);
    when(trainerRepository.findByEmail(trainerEmail)).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.empty());

    // when / then
    assertThrows(TraineeNotExistException.class, () -> trainerService.editTraineeInfo(dto));
    verify(trainerRepository, times(1)).findByEmail(trainerEmail);
    verify(traineeRepository, times(1)).findById(traineeId);
  }

  @Test
  @DisplayName("인바디 기록 추가 성공")
  void testAddInBodyRecord_Success() {
    // given
    Long traineeId = 1L;
    Long trainerId = 1L;
    String trainerEmail = "trainer@example.com";

    TrainerEntity trainer = new TrainerEntity();
    trainer.setId(trainerId);

    TraineeEntity trainee = new TraineeEntity();
    trainee.setId(traineeId);

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

    when(authentication.getName()).thenReturn(trainerEmail);
    when(trainerRepository.findByEmail(trainerEmail)).thenReturn(Optional.of(trainer));
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

    verify(trainerRepository, times(1)).findByEmail(trainerEmail);
    verify(traineeRepository, times(1)).findById(traineeId);
    verify(ptContractRepository, times(1)).existsByTrainerIdAndTraineeId(trainerId, traineeId);
    verify(inBodyRecordHistoryRepository, times(1)).save(any(InBodyRecordHistoryEntity.class));
  }

  @Test
  @DisplayName("인바디 기록 추가 시 트레이니가 존재하지 않을 때 예외 발생")
  void testAddInBodyRecord_TraineeNotExistException() {
    // given
    Long traineeId = 1L;
    String trainerEmail = "trainer@example.com";

    TrainerEntity trainer = new TrainerEntity();
    trainer.setId(1L);

    AddInBodyInfoRequestDto dto = new AddInBodyInfoRequestDto();
    dto.setTraineeId(traineeId);

    when(authentication.getName()).thenReturn(trainerEmail);
    when(trainerRepository.findByEmail(trainerEmail)).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.empty());

    // when / then
    assertThrows(TraineeNotExistException.class, () -> trainerService.addInBodyRecord(dto));
    verify(trainerRepository, times(1)).findByEmail(trainerEmail);
    verify(traineeRepository, times(1)).findById(traineeId);

  }

  @Test
  @DisplayName("트레이너와 트레이니 사이에 계약이 존재하지 않을 때 예외 발생")
  void testGetTraineeInfo_ContractNotExist() {
    // given
    Long traineeId = 1L;
    Long trainerId = 1L;
    String trainerEmail = "trainer@example.com";

    TrainerEntity trainer = new TrainerEntity();
    trainer.setId(trainerId);

    TraineeEntity trainee = new TraineeEntity();
    trainee.setId(traineeId);

    when(authentication.getName()).thenReturn(trainerEmail);
    when(trainerRepository.findByEmail(trainerEmail)).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
    when(ptContractRepository.existsByTrainerIdAndTraineeId(trainerId, traineeId)).thenReturn(
        false);

    // when / then
    assertThrows(PtContractNotExistException.class, () -> trainerService.getTraineeInfo(traineeId));
  }

  @Test
  @DisplayName("트레이너와 트레이니 사이에 계약이 존재하지 않을 때 인바디 기록 추가 시 예외 발생")
  void testAddInBodyRecord_ContractNotExist() {
    // given
    Long traineeId = 1L;
    Long trainerId = 1L;
    String trainerEmail = "trainer@example.com";

    TrainerEntity trainer = new TrainerEntity();
    trainer.setId(trainerId);

    TraineeEntity trainee = new TraineeEntity();
    trainee.setId(traineeId);

    AddInBodyInfoRequestDto dto = new AddInBodyInfoRequestDto();
    dto.setTraineeId(traineeId);

    when(authentication.getName()).thenReturn(trainerEmail);
    when(trainerRepository.findByEmail(trainerEmail)).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
    when(ptContractRepository.existsByTrainerIdAndTraineeId(trainerId, traineeId)).thenReturn(
        false);

    // when / then
    assertThrows(PtContractNotExistException.class, () -> trainerService.addInBodyRecord(dto));
  }

  @Test
  @DisplayName("트레이너와 트레이니 사이에 계약이 존재하지 않을 때 트레이니 정보 수정 시 예외 발생")
  void testEditTraineeInfo_ContractNotExist() {
    // given
    Long traineeId = 1L;
    Long trainerId = 1L;
    String trainerEmail = "trainer@example.com";

    TrainerEntity trainer = new TrainerEntity();
    trainer.setId(trainerId);

    TraineeEntity trainee = new TraineeEntity();
    trainee.setId(traineeId);

    EditTraineeInfoRequestDto dto = new EditTraineeInfoRequestDto();
    dto.setTraineeId(traineeId);

    when(authentication.getName()).thenReturn(trainerEmail);
    when(trainerRepository.findByEmail(trainerEmail)).thenReturn(Optional.of(trainer));
    when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
    when(ptContractRepository.existsByTrainerIdAndTraineeId(trainerId, traineeId)).thenReturn(
        false);

    // when / then
    assertThrows(PtContractNotExistException.class, () -> trainerService.editTraineeInfo(dto));
  }

}