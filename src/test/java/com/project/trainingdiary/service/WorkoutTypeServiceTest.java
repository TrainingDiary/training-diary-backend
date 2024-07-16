package com.project.trainingdiary.service;

import static com.project.trainingdiary.model.UserRoleType.TRAINER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.WorkoutTypeCreateRequestDto;
import com.project.trainingdiary.dto.request.WorkoutTypeUpdateRequestDto;
import com.project.trainingdiary.dto.response.WorkoutTypeResponseDto;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutTypeEntity;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.exception.impl.WorkoutTypeNotFoundException;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.WorkoutTypeRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class WorkoutTypeServiceTest {

  @Mock
  private WorkoutTypeRepository workoutTypeRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @InjectMocks
  private WorkoutTypeService workoutTypeService;

  private TrainerEntity trainer;
  private WorkoutTypeEntity workoutType;
  private WorkoutTypeCreateRequestDto createRequestDto;
  private WorkoutTypeUpdateRequestDto updateRequestDto;

  @BeforeEach
  void init() {
    trainer = TrainerEntity.builder()
        .id(1L)
        .email("trainer@gmail.com")
        .role(TRAINER)
        .build();

    workoutType = WorkoutTypeEntity.builder()
        .id(10L)
        .name("type1")
        .targetMuscle("target1")
        .remarks("remark1")
        .trainer(trainer)
        .build();

    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
  }

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("운동 종류 생성 성공")
  void testCreateWorkoutTypeSuccess() {
    createRequestDto = new WorkoutTypeCreateRequestDto();
    createRequestDto.setName("type2");
    createRequestDto.setTargetMuscle("target2");
    createRequestDto.setRemarks("remark2");

    WorkoutTypeEntity newWorkoutType = WorkoutTypeEntity.builder()
        .id(11L)
        .name(createRequestDto.getName())
        .targetMuscle(createRequestDto.getTargetMuscle())
        .remarks(createRequestDto.getRemarks())
        .trainer(trainer)
        .build();

    ArgumentCaptor<WorkoutTypeEntity> captor = ArgumentCaptor.forClass(WorkoutTypeEntity.class);
    when(workoutTypeRepository.save(captor.capture())).thenReturn(newWorkoutType);
    WorkoutTypeResponseDto responseDto = workoutTypeService.createWorkoutType(createRequestDto);

    assertNotNull(responseDto);
    assertEquals(11L, responseDto.getId());
    assertEquals("type2", responseDto.getName());

    WorkoutTypeEntity capturedWorkoutType = captor.getValue();
    assertEquals("type2", capturedWorkoutType.getName());
    assertEquals("target2", capturedWorkoutType.getTargetMuscle());
    assertEquals("remark2", capturedWorkoutType.getRemarks());
    assertEquals(trainer, capturedWorkoutType.getTrainer());

    verify(workoutTypeRepository, times(1)).save(capturedWorkoutType);
  }

  @Test
  @DisplayName("운동 종류 생성 실패 - 트레이너를 찾을 수 없을 때 예외 발생")
  void testCreateWorkoutTypeFail() {
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class,
        () -> workoutTypeService.createWorkoutType(createRequestDto));
  }

  @Test
  @DisplayName("운동 종류 수정 성공")
  void testUpdateWorkoutTypeSuccess() {
    updateRequestDto = new WorkoutTypeUpdateRequestDto();
    updateRequestDto.setWorkoutTypeId(10L);
    updateRequestDto.setName("type2");
    updateRequestDto.setTargetMuscle("target2");
    updateRequestDto.setRemarks("remark2");

    WorkoutTypeEntity updateWorkoutType = WorkoutTypeEntity.builder()
        .id(10L)
        .name(updateRequestDto.getName())
        .targetMuscle(updateRequestDto.getTargetMuscle())
        .remarks(updateRequestDto.getRemarks())
        .trainer(trainer)
        .build();

    when(workoutTypeRepository.findByTrainer_IdAndId(trainer.getId(), updateWorkoutType.getId()))
        .thenReturn(Optional.of(workoutType));

    ArgumentCaptor<WorkoutTypeEntity> captor = ArgumentCaptor.forClass(WorkoutTypeEntity.class);
    when(workoutTypeRepository.save(captor.capture())).thenReturn(updateWorkoutType);
    WorkoutTypeResponseDto response = workoutTypeService.updateWorkoutType(updateRequestDto);

    assertNotNull(response);
    assertEquals("type2", response.getName());

    WorkoutTypeEntity capturedWorkoutType = captor.getValue();
    assertEquals(10L, capturedWorkoutType.getId());
    assertEquals("type2", capturedWorkoutType.getName());
    assertEquals("target2", capturedWorkoutType.getTargetMuscle());
    assertEquals("remark2", capturedWorkoutType.getRemarks());
    assertEquals(trainer, capturedWorkoutType.getTrainer());

    verify(workoutTypeRepository, times(1)).save(capturedWorkoutType);
  }

  @Test
  @DisplayName("운동 종류 수정 실패 - 운동 종류 id 존재하지 않을 때 예외 발생")
  void testUpdateWorkoutTypeFailWorkoutTypeNotFound() {
    updateRequestDto = new WorkoutTypeUpdateRequestDto();
    updateRequestDto.setWorkoutTypeId(99L);

    when(workoutTypeRepository.findByTrainer_IdAndId(trainer.getId(), 99L))
        .thenReturn(Optional.empty());

    assertThrows(WorkoutTypeNotFoundException.class,
        () -> workoutTypeService.updateWorkoutType(updateRequestDto));
  }

  @Test
  @DisplayName("운동 종류 삭제 성공")
  void testDeleteWorkoutTypeSuccess() {
    when(workoutTypeRepository.findByTrainer_IdAndId(trainer.getId(), 10L))
        .thenReturn(Optional.of(workoutType));

    doNothing().when(workoutTypeRepository).delete(workoutType);

    workoutTypeService.deleteWorkoutType(10L);

    verify(workoutTypeRepository, times(1)).delete(workoutType);
  }

  @Test
  @DisplayName("운동 종류 삭제 실패 - 운동 종류 id 존재하지 않을 때 예외 발생")
  void testDeleteWorkoutTypeFailWorkoutTypeNotFound() {
    when(workoutTypeRepository.findByTrainer_IdAndId(trainer.getId(), 99L))
        .thenReturn(Optional.empty());

    assertThrows(WorkoutTypeNotFoundException.class,
        () -> workoutTypeService.deleteWorkoutType(99L));
  }

  @Test
  @DisplayName("운동 종류 목록 조회 성공")
  void testGetWorkoutTypesSuccess() {
    Page<WorkoutTypeEntity> workoutTypePage = new PageImpl<>(
        Collections.singletonList(workoutType));

    when(workoutTypeRepository
        .findByTrainer_IdOrderByCreatedAtDesc(trainer.getId(), Pageable.unpaged()))
        .thenReturn(workoutTypePage);

    Page<WorkoutTypeResponseDto> responsePageDto = workoutTypeService
        .getWorkoutTypes(Pageable.unpaged());

    assertNotNull(responsePageDto);
    assertEquals(1, responsePageDto.getTotalElements());
    assertEquals("type1", responsePageDto.getContent().get(0).getName());
  }

  @Test
  @DisplayName("운동 종류 목록 조회 실패 - 등록한 운동 종류가 없을 때")
  void testGetWorkoutTypesFailure_NoWorkoutTypes() {
    Page<WorkoutTypeEntity> emptyPage = new PageImpl<>(Collections.emptyList());

    when(workoutTypeRepository
        .findByTrainer_IdOrderByCreatedAtDesc(trainer.getId(), Pageable.unpaged()))
        .thenReturn(emptyPage);

    Page<WorkoutTypeResponseDto> responsePageDto = workoutTypeService.getWorkoutTypes(
        Pageable.unpaged());

    assertNotNull(responsePageDto);
    assertEquals(0, responsePageDto.getTotalElements());
    assertTrue(responsePageDto.getContent().isEmpty());
  }

  @Test
  @DisplayName("운동 종류 상세 조회 성공")
  void testGetWorkoutTypeDetailsSuccess() {
    when(workoutTypeRepository.findByTrainer_IdAndId(trainer.getId(), 10L))
        .thenReturn(Optional.of(workoutType));

    WorkoutTypeResponseDto responseDto = workoutTypeService.getWorkoutTypeDetails(10L);

    assertNotNull(responseDto);
    assertEquals("type1", responseDto.getName());
  }

  @Test
  @DisplayName("운동 종류 상세 조회 실패 - 운동 종류 id 존재하지 않을 때 예외 발생")
  void testGetWorkoutTypeDetailsFailWorkoutTypeNotFound() {
    when(workoutTypeRepository.findByTrainer_IdAndId(trainer.getId(), 99L))
        .thenReturn(Optional.empty());

    assertThrows(WorkoutTypeNotFoundException.class,
        () -> workoutTypeService.getWorkoutTypeDetails(99L));
  }

}