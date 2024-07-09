package com.project.trainingdiary.service;

import static com.project.trainingdiary.model.UserRoleType.TRAINEE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.WorkoutCreateRequestDto;
import com.project.trainingdiary.dto.request.WorkoutSessionCreateRequestDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutEntity;
import com.project.trainingdiary.entity.WorkoutSessionEntity;
import com.project.trainingdiary.entity.WorkoutTypeEntity;
import com.project.trainingdiary.exception.impl.PtContractNotFoundException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.exception.impl.WorkoutTypeNotFoundException;
import com.project.trainingdiary.repository.PtContractRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.WorkoutRepository;
import com.project.trainingdiary.repository.WorkoutSessionRepository;
import com.project.trainingdiary.repository.WorkoutTypeRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class WorkoutSessionServiceTest {

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private PtContractRepository ptContractRepository;

  @Mock
  private WorkoutTypeRepository workoutTypeRepository;

  @Mock
  private WorkoutRepository workoutRepository;

  @Mock
  private WorkoutSessionRepository workoutSessionRepository;

  @InjectMocks
  private WorkoutSessionService workoutSessionService;

  private TrainerEntity trainer;
  private PtContractEntity ptContract;
  private WorkoutTypeEntity workoutType;

  @BeforeEach
  public void init() {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    trainer = TrainerEntity.builder().id(1L).email("trainer@gmail.com").role(TRAINEE).build();
    ptContract = PtContractEntity.builder().id(100L).trainer(trainer)
        .trainee(TraineeEntity.builder().id(10L).role(TRAINEE).build()).build();
    workoutType = WorkoutTypeEntity.builder().id(1000L).name("WorkoutType").build();
  }

  @Test
  @DisplayName("운동 일지 생성 성공")
  public void testCreateWorkoutSessionSuccess() {
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), 10L)).thenReturn(
        Optional.of(ptContract));
    when(workoutTypeRepository.findById(1000L)).thenReturn(Optional.of(workoutType));

    WorkoutCreateRequestDto workoutCreateRequestDto = new WorkoutCreateRequestDto();
    workoutCreateRequestDto.setWorkoutTypeId(1000L);

    WorkoutSessionCreateRequestDto workoutSessionCreateRequestDto = new WorkoutSessionCreateRequestDto();
    workoutSessionCreateRequestDto.setWorkouts(List.of(workoutCreateRequestDto));

    workoutSessionService.createWorkoutSession(workoutSessionCreateRequestDto, 10L);

    ArgumentCaptor<WorkoutEntity> workoutCaptor = ArgumentCaptor.forClass(WorkoutEntity.class);
    verify(workoutRepository, times(1)).save(workoutCaptor.capture());
    assertEquals(workoutType, workoutCaptor.getValue().getWorkoutType());

    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor = ArgumentCaptor.forClass(
        WorkoutSessionEntity.class);
    verify(workoutSessionRepository, times(1)).save(sessionCaptor.capture());
    assertEquals(ptContract, sessionCaptor.getValue().getPtContract());
    assertEquals(1, sessionCaptor.getValue().getWorkouts().size());
    assertEquals(workoutType, sessionCaptor.getValue().getWorkouts().get(0).getWorkoutType());
  }

  @Test
  @DisplayName("운동 일지 생성 실패 - 트레이너를 찾지 못할 때 예외 발생")
  public void testCreateWorkoutSessionFailTrainerNotFound() {
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.empty());

    WorkoutCreateRequestDto workoutCreateRequestDto = new WorkoutCreateRequestDto();
    workoutCreateRequestDto.setWorkoutTypeId(1000L);

    WorkoutSessionCreateRequestDto workoutSessionCreateRequestDto = new WorkoutSessionCreateRequestDto();
    workoutSessionCreateRequestDto.setWorkouts(List.of(workoutCreateRequestDto));

    assertThrows(UserNotFoundException.class,
        () -> workoutSessionService.createWorkoutSession(workoutSessionCreateRequestDto, 10L));

    verify(trainerRepository, times(1)).findByEmail("trainer@gmail.com");
    verifyNoInteractions(ptContractRepository, workoutTypeRepository, workoutRepository,
        workoutSessionRepository);
  }

  @Test
  @DisplayName("운동 일지 생성 실패 - PT 계약이 존재 하지 않을 때 예외 발생")
  public void testCreateWorkoutSessionFailPtContractNotFound() {
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), 10L)).thenReturn(
        Optional.empty());

    WorkoutCreateRequestDto workoutCreateRequestDto = new WorkoutCreateRequestDto();
    workoutCreateRequestDto.setWorkoutTypeId(1000L);

    WorkoutSessionCreateRequestDto workoutSessionCreateRequestDto = new WorkoutSessionCreateRequestDto();
    workoutSessionCreateRequestDto.setWorkouts(List.of(workoutCreateRequestDto));

    assertThrows(PtContractNotFoundException.class,
        () -> workoutSessionService.createWorkoutSession(workoutSessionCreateRequestDto, 10L));

    verify(trainerRepository, times(1)).findByEmail("trainer@gmail.com");
    verify(ptContractRepository, times(1)).findByTrainerIdAndTraineeId(trainer.getId(), 10L);
    verifyNoInteractions(workoutTypeRepository, workoutRepository, workoutSessionRepository);
  }

  @Test
  @DisplayName("운동 일지 생성 실패 - 운동 종류가 존재 하지 않을 때 예외 발생")
  public void testCreateWorkoutSessionFailWorkoutTypeNotFound() {
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), 10L)).thenReturn(
        Optional.of(ptContract));
    when(workoutTypeRepository.findById(1000L)).thenReturn(Optional.empty());

    WorkoutCreateRequestDto workoutCreateRequestDto = new WorkoutCreateRequestDto();
    workoutCreateRequestDto.setWorkoutTypeId(1000L);

    WorkoutSessionCreateRequestDto workoutSessionCreateRequestDto = new WorkoutSessionCreateRequestDto();
    workoutSessionCreateRequestDto.setWorkouts(List.of(workoutCreateRequestDto));

    assertThrows(WorkoutTypeNotFoundException.class,
        () -> workoutSessionService.createWorkoutSession(workoutSessionCreateRequestDto, 10L));

    verify(trainerRepository, times(1)).findByEmail("trainer@gmail.com");
    verify(ptContractRepository, times(1)).findByTrainerIdAndTraineeId(trainer.getId(), 10L);
    verify(workoutTypeRepository, times(1)).findById(1000L);
    verifyNoInteractions(workoutRepository, workoutSessionRepository);
  }

}