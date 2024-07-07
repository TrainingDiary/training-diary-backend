package com.project.trainingdiary.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.WorkoutTypeCreateRequestDto;
import com.project.trainingdiary.dto.request.WorkoutTypeUpdateRequestDto;
import com.project.trainingdiary.dto.response.WorkoutTypeResponseDto;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutTypeEntity;
import com.project.trainingdiary.exception.impl.TrainerIdNotFoundException;
import com.project.trainingdiary.exception.impl.WorkoutTypeNotFoundException;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.WorkoutTypeRepository;
import com.project.trainingdiary.service.WorkoutTypeService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class WorkoutTypeServiceImplTest {

  @Mock
  private WorkoutTypeRepository workoutTypeRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @InjectMocks
  private WorkoutTypeService workoutTypeService;

  private TrainerEntity trainerEntity;
  private WorkoutTypeEntity workoutTypeEntity;
  private WorkoutTypeCreateRequestDto createRequestDto;
  private WorkoutTypeUpdateRequestDto updateRequestDto;

  @BeforeEach
  void init() {
    trainerEntity = TrainerEntity.builder().id(1L).build();
    workoutTypeEntity = WorkoutTypeEntity.builder().id(1L).trainer(trainerEntity).build();
    createRequestDto = new WorkoutTypeCreateRequestDto(1L, "workout1", "targetA", "remarks", true,
        true, true, false, false);
    updateRequestDto = new WorkoutTypeUpdateRequestDto("workout1", "targetB", "remarks");
  }

  @Test()
  @DisplayName("운동 종류 생성 성공")
  void testCreateWorkoutTypeSuccess() {
    when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainerEntity));
    when(workoutTypeRepository.save(any(WorkoutTypeEntity.class))).thenReturn(workoutTypeEntity);

    workoutTypeService.createWorkoutType(createRequestDto);

    verify(trainerRepository, times(1)).findById(1L);
    verify(workoutTypeRepository, times(1)).save(any(WorkoutTypeEntity.class));
  }

  @Test()
  @DisplayName("운동 종류 생성 실패 - 트레이너가 존재하지 않을 때 예외 발생")
  void testCreateWorkoutTypeFail() {
    when(trainerRepository.findById(1L)).thenReturn(Optional.empty());
    TrainerIdNotFoundException exception = assertThrows(TrainerIdNotFoundException.class,
        () -> workoutTypeService.createWorkoutType(createRequestDto));

    assertEquals("해당 일련번호의 트레이너를 찾을 수 없습니다. ID: 1", exception.getMessage());
    verify(trainerRepository, times(1)).findById(1L);
    verify(workoutTypeRepository, times(0)).save(any(WorkoutTypeEntity.class));
  }

  @Test()
  @DisplayName("운동 종류 수정 성공")
  void testUpdateWorkoutTypeSuccess() {
    when(workoutTypeRepository.findByTrainer_IdAndId(1L, 1L)).thenReturn(
        Optional.of(workoutTypeEntity));
    when(workoutTypeRepository.save(any(WorkoutTypeEntity.class))).thenReturn(workoutTypeEntity);

    workoutTypeService.updateWorkoutType(1L, 1L, updateRequestDto);

    verify(workoutTypeRepository, times(1)).findByTrainer_IdAndId(1L, 1L);
    verify(workoutTypeRepository, times(1)).save(any(WorkoutTypeEntity.class));
  }

  @Test()
  @DisplayName("운동 종류 수정 실패 - 운동 종류가 존재하지 않을 때 예외 발생")
  void testUpdateWorkoutTypeFail() {
    when(workoutTypeRepository.findByTrainer_IdAndId(1L, 1L)).thenReturn(Optional.empty());
    WorkoutTypeNotFoundException exception = assertThrows(WorkoutTypeNotFoundException.class,
        () -> workoutTypeService.updateWorkoutType(1L, 1L, updateRequestDto));

    assertEquals("해당 일련번호의 운동 종류를 찾을 수 없습니다. ID: 1", exception.getMessage());
    verify(workoutTypeRepository, times(1)).findByTrainer_IdAndId(1L, 1L);
    verify(workoutTypeRepository, times(0)).save(any(WorkoutTypeEntity.class));
  }

  @Test()
  @DisplayName("운동 종류 삭제 성공")
  void testDeleteWorkoutTypeSuccess() {
    when(workoutTypeRepository.findByTrainer_IdAndId(1L, 1L)).thenReturn(
        Optional.of(workoutTypeEntity));

    workoutTypeService.deleteWorkoutType(1L, 1L);

    verify(workoutTypeRepository, times(1)).findByTrainer_IdAndId(1L, 1L);
    verify(workoutTypeRepository, times(1)).delete(any(WorkoutTypeEntity.class));
  }

  @Test()
  @DisplayName("운동 종류 삭제 실패 - 운동 종류가 존재하지 않을 때 예외 발생")
  void testDeleteWorkoutTypeFail() {
    when(workoutTypeRepository.findByTrainer_IdAndId(1L, 1L)).thenReturn(Optional.empty());
    WorkoutTypeNotFoundException exception = assertThrows(WorkoutTypeNotFoundException.class,
        () -> workoutTypeService.updateWorkoutType(1L, 1L, updateRequestDto));

    assertEquals("해당 일련번호의 운동 종류를 찾을 수 없습니다. ID: 1", exception.getMessage());
    verify(workoutTypeRepository, times(1)).findByTrainer_IdAndId(1L, 1L);
    verify(workoutTypeRepository, times(0)).save(any(WorkoutTypeEntity.class));
  }

  @Test
  @DisplayName("운동 종류 목록 조회 성공")
  void testGetWorkoutTypesSuccess() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<WorkoutTypeEntity> page = new PageImpl<>(List.of(workoutTypeEntity));

    when(workoutTypeRepository.findByTrainer_Id(1L, pageable)).thenReturn(page);

    Page<WorkoutTypeResponseDto> responseDtos = workoutTypeService.getWorkoutTypes(1L, pageable);

    assertEquals(1, responseDtos.getTotalElements());
    verify(workoutTypeRepository, times(1)).findByTrainer_Id(1L, pageable);
  }

  @Test
  @DisplayName("운동 종류 목록 조회 실패 - 해당 트레이너가 등록한 운동 종류가 없을 때")
  void testGetWorkoutTypesFail() {
    Pageable pageable = PageRequest.of(0, 10);
    when(workoutTypeRepository.findByTrainer_Id(1L, pageable)).thenReturn(Page.empty());

    Page<WorkoutTypeResponseDto> responseDtos = workoutTypeService.getWorkoutTypes(1L, pageable);

    assertEquals(0, responseDtos.getTotalElements());
    verify(workoutTypeRepository, times(1)).findByTrainer_Id(1L, pageable);
  }

  @Test
  @DisplayName("운동 종류 상세 조회 성공")
  void testGetWorkoutTypeDetailsSuccess() {
    when(workoutTypeRepository.findByTrainer_IdAndId(1L, 1L)).thenReturn(
        Optional.of(workoutTypeEntity));

    WorkoutTypeResponseDto responseDto = workoutTypeService.getWorkoutTypeDetails(1L, 1L);

    assertNotNull(responseDto);
    verify(workoutTypeRepository, times(1)).findByTrainer_IdAndId(1L, 1L);
  }

  @Test
  @DisplayName("운동 종류 상세 조회 실패 - 운동 종류를 찾지 못했을 때 예외 발생")
  void testGetWorkoutTypeDetailsFail() {
    when(workoutTypeRepository.findByTrainer_IdAndId(1L, 1L)).thenReturn(Optional.empty());
    WorkoutTypeNotFoundException exception = assertThrows(WorkoutTypeNotFoundException.class,
        () -> workoutTypeService.getWorkoutTypeDetails(1L, 1L));

    assertEquals("해당 일련번호의 운동 종류를 찾을 수 없습니다. ID: 1", exception.getMessage());
    verify(workoutTypeRepository, times(1)).findByTrainer_IdAndId(1L, 1L);
  }

}