package com.project.trainingdiary.service;

import static com.project.trainingdiary.model.UserRoleType.TRAINEE;
import static com.project.trainingdiary.model.UserRoleType.TRAINER;
import static com.project.trainingdiary.model.WorkoutMediaType.IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.WorkoutDto;
import com.project.trainingdiary.dto.request.WorkoutImageRequestDto;
import com.project.trainingdiary.dto.request.WorkoutSessionCreateRequestDto;
import com.project.trainingdiary.dto.response.WorkoutImageResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionListResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutEntity;
import com.project.trainingdiary.entity.WorkoutMediaEntity;
import com.project.trainingdiary.entity.WorkoutSessionEntity;
import com.project.trainingdiary.entity.WorkoutTypeEntity;
import com.project.trainingdiary.exception.impl.InvalidFileTypeException;
import com.project.trainingdiary.exception.impl.MediaCountExceededException;
import com.project.trainingdiary.exception.impl.PtContractNotFoundException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.exception.impl.WorkoutSessionNotFoundException;
import com.project.trainingdiary.exception.impl.WorkoutTypeNotFoundException;
import com.project.trainingdiary.repository.PtContractRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.WorkoutMediaRepository;
import com.project.trainingdiary.repository.WorkoutRepository;
import com.project.trainingdiary.repository.WorkoutSessionRepository;
import com.project.trainingdiary.repository.WorkoutTypeRepository;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestPropertySource("classpath:application-test.yml")
class WorkoutSessionServiceTest {

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

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

  @Mock
  private WorkoutMediaRepository workoutMediaRepository;

  @Mock
  private S3Operations s3Operations;


  @InjectMocks
  private WorkoutSessionService workoutSessionService;

  private TrainerEntity trainer;
  private TraineeEntity trainee;
  private PtContractEntity ptContract;
  private WorkoutTypeEntity workoutType;
  private WorkoutSessionEntity workoutSession;

  @BeforeEach
  void init() {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    trainer = TrainerEntity.builder().id(1L).email("trainer@gmail.com").role(TRAINER).build();
    trainee = TraineeEntity.builder().id(10L).role(TRAINEE).build();
    ptContract = PtContractEntity.builder().id(100L).trainer(trainer)
        .trainee(trainee).build();
    workoutType = WorkoutTypeEntity.builder().id(1000L).name("WorkoutType").build();
    workoutSession = WorkoutSessionEntity.builder().id(10000L).sessionDate(LocalDate.now())
        .sessionNumber(1).ptContract(ptContract).workouts(new ArrayList<>())
        .workoutMedia(new ArrayList<>()).build();
  }

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("운동 일지 생성 성공")
  void testCreateWorkoutSessionSuccess() {
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), trainee.getId()))
        .thenReturn(Optional.of(ptContract));
    when(workoutTypeRepository.findById(1000L)).thenReturn(Optional.of(workoutType));

    WorkoutDto workoutDto = new WorkoutDto();
    workoutDto.setWorkoutTypeId(1000L);

    WorkoutSessionCreateRequestDto workoutSessionCreateRequestDto = new WorkoutSessionCreateRequestDto();
    workoutSessionCreateRequestDto.setTraineeId(10L);
    workoutSessionCreateRequestDto.setWorkouts(List.of(workoutDto));

    workoutSessionService.createWorkoutSession(workoutSessionCreateRequestDto);

    ArgumentCaptor<WorkoutEntity> workoutCaptor = ArgumentCaptor.forClass(WorkoutEntity.class);
    verify(workoutRepository, times(1)).save(workoutCaptor.capture());
    assertEquals("WorkoutType", workoutCaptor.getValue().getWorkoutTypeName());

    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor =
        ArgumentCaptor.forClass(WorkoutSessionEntity.class);
    verify(workoutSessionRepository, times(1)).save(sessionCaptor.capture());
    assertEquals(ptContract, sessionCaptor.getValue().getPtContract());
    assertEquals(1, sessionCaptor.getValue().getWorkouts().size());
    assertEquals("WorkoutType", sessionCaptor.getValue().getWorkouts().get(0).getWorkoutTypeName());
  }

  @Test
  @DisplayName("운동 일지 생성 실패 - 트레이너를 찾지 못할 때 예외 발생")
  void testCreateWorkoutSessionFailTrainerNotFound() {
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.empty());

    WorkoutDto workoutDto = new WorkoutDto();
    workoutDto.setWorkoutTypeId(1000L);

    WorkoutSessionCreateRequestDto workoutSessionCreateRequestDto = new WorkoutSessionCreateRequestDto();
    workoutSessionCreateRequestDto.setTraineeId(10L);
    workoutSessionCreateRequestDto.setWorkouts(List.of(workoutDto));

    assertThrows(UserNotFoundException.class,
        () -> workoutSessionService.createWorkoutSession(workoutSessionCreateRequestDto));

    verify(trainerRepository, times(1)).findByEmail("trainer@gmail.com");
    verifyNoInteractions(ptContractRepository, workoutTypeRepository, workoutRepository,
        workoutSessionRepository);
  }

  @Test
  @DisplayName("운동 일지 생성 실패 - PT 계약이 존재 하지 않을 때 예외 발생")
  void testCreateWorkoutSessionFailPtContractNotFound() {
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), 10L))
        .thenReturn(Optional.empty());

    WorkoutDto workoutDto = new WorkoutDto();
    workoutDto.setWorkoutTypeId(1000L);

    WorkoutSessionCreateRequestDto workoutSessionCreateRequestDto = new WorkoutSessionCreateRequestDto();
    workoutSessionCreateRequestDto.setTraineeId(10L);
    workoutSessionCreateRequestDto.setWorkouts(List.of(workoutDto));

    assertThrows(PtContractNotFoundException.class,
        () -> workoutSessionService.createWorkoutSession(workoutSessionCreateRequestDto));

    verify(trainerRepository, times(1)).findByEmail("trainer@gmail.com");
    verify(ptContractRepository, times(1)).findByTrainerIdAndTraineeId(trainer.getId(), 10L);
    verifyNoInteractions(workoutTypeRepository, workoutRepository, workoutSessionRepository);
  }

  @Test
  @DisplayName("운동 일지 생성 실패 - 운동 종류가 존재 하지 않을 때 예외 발생")
  void testCreateWorkoutSessionFailWorkoutTypeNotFound() {
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), 10L))
        .thenReturn(Optional.of(ptContract));
    when(workoutTypeRepository.findById(1000L)).thenReturn(Optional.empty());

    WorkoutDto workoutDto = new WorkoutDto();
    workoutDto.setWorkoutTypeId(1000L);

    WorkoutSessionCreateRequestDto workoutSessionCreateRequestDto = new WorkoutSessionCreateRequestDto();
    workoutSessionCreateRequestDto.setTraineeId(10L);
    workoutSessionCreateRequestDto.setWorkouts(List.of(workoutDto));

    assertThrows(WorkoutTypeNotFoundException.class,
        () -> workoutSessionService.createWorkoutSession(workoutSessionCreateRequestDto));

    verify(trainerRepository, times(1)).findByEmail("trainer@gmail.com");
    verify(ptContractRepository, times(1)).findByTrainerIdAndTraineeId(trainer.getId(), 10L);
    verify(workoutTypeRepository, times(1)).findById(1000L);
    verifyNoInteractions(workoutRepository, workoutSessionRepository);
  }

  @Test
  @DisplayName("운동 일지 목록 조회 성공")
  void testGetWorkoutSessionsSuccess() {
    Pageable pageable = PageRequest.of(0, 10);

    when(workoutSessionRepository
        .findByPtContract_Trainee_IdOrderBySessionDateDesc(trainee.getId(), pageable))
        .thenReturn(new PageImpl<>(List.of(workoutSession)));

    Page<WorkoutSessionListResponseDto> result = workoutSessionService
        .getWorkoutSessions(trainee.getId(), pageable);

    assertEquals(1, result.getTotalElements());
    assertEquals(workoutSession.getId(), result.getContent().get(0).getId());

    verify(workoutSessionRepository, times(1))
        .findByPtContract_Trainee_IdOrderBySessionDateDesc(trainee.getId(), pageable);
  }

  @Test
  @DisplayName("운동 일지 목록 조회 실패 - 트레이니를 찾지 못할 때 예외 발생")
  void testGetWorkoutSessionsFailInvalidTraineeId() {
    Pageable pageable = PageRequest.of(0, 10);

    when(workoutSessionRepository
        .findByPtContract_Trainee_IdOrderBySessionDateDesc(trainee.getId(), pageable))
        .thenReturn(Page.empty());

    Page<WorkoutSessionListResponseDto> result = workoutSessionService
        .getWorkoutSessions(trainee.getId(), pageable);

    assertEquals(0, result.getTotalElements());

    verify(workoutSessionRepository, times(1))
        .findByPtContract_Trainee_IdOrderBySessionDateDesc(trainee.getId(), pageable);
  }

  @Test
  @DisplayName("운동 일지 상세 조회 성공")
  void testGetWorkoutSessionDetailsSuccess() {
    when(workoutSessionRepository
        .findByIdAndPtContract_Trainee_Id(workoutSession.getId(), trainee.getId()))
        .thenReturn(Optional.of(workoutSession));

    WorkoutSessionResponseDto result = workoutSessionService
        .getWorkoutSessionDetails(trainee.getId(), workoutSession.getId());

    assertEquals(workoutSession.getId(), result.getId());
    assertEquals(workoutSession.getSessionDate(), result.getSessionDate());
    assertEquals(workoutSession.getSessionNumber(), result.getSessionNumber());

    verify(workoutSessionRepository, times(1))
        .findByIdAndPtContract_Trainee_Id(workoutSession.getId(), trainee.getId());
  }

  @Test
  @DisplayName("운동 일지 상세 조회 실패 - 운동 일지가 존재하지 않을 때 예외 발생")
  void testGetWorkoutSessionDetailsFailInvalidSessionId() {
    when(workoutSessionRepository.findByIdAndPtContract_Trainee_Id(1L, trainee.getId()))
        .thenReturn(Optional.empty());

    assertThrows(WorkoutSessionNotFoundException.class,
        () -> workoutSessionService.getWorkoutSessionDetails(trainee.getId(), 1L));

    verify(workoutSessionRepository, times(1))
        .findByIdAndPtContract_Trainee_Id(1L, trainee.getId());
  }

  @Test
  @DisplayName("이미지 업로드 성공")
  void testUploadWorkoutImageSuccess() throws IOException {
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(workoutSessionRepository.findByPtContract_TrainerAndId(trainer, 10000L))
        .thenReturn(Optional.of(workoutSession));

    byte[] imageBytes = Files.readAllBytes(
        Paths.get(System.getProperty("user.home") + "/Downloads/test.jpeg"));
    MockMultipartFile mockFile = new MockMultipartFile(
        "file", "test.jpg", "image/jpeg", imageBytes);

    WorkoutImageRequestDto dto = WorkoutImageRequestDto.builder()
        .sessionId(10000L)
        .images(List.of(mockFile))
        .build();

    S3Resource s3ResourceOriginal = mock(S3Resource.class);
    S3Resource s3ResourceThumbnail = mock(S3Resource.class);

    when(s3ResourceOriginal.getURL()).thenReturn(
        new URL("https://test-bucket.s3.amazonaws.com/original"));
    when(s3ResourceThumbnail.getURL()).thenReturn(
        new URL("https://test-bucket.s3.amazonaws.com/thumbnail"));

    ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);

    doAnswer(invocation -> s3ResourceOriginal).when(s3Operations)
        .upload(bucketCaptor.capture(), keyCaptor.capture(), inputStreamCaptor.capture(),
            metadataCaptor.capture());

    doAnswer(invocation -> s3ResourceThumbnail).when(s3Operations)
        .upload(bucketCaptor.capture(), keyCaptor.capture(), inputStreamCaptor.capture(),
            metadataCaptor.capture());

    WorkoutImageResponseDto response = workoutSessionService.uploadWorkoutImage(dto);

    ArgumentCaptor<WorkoutMediaEntity> mediaCaptor = ArgumentCaptor.forClass(
        WorkoutMediaEntity.class);
    verify(workoutMediaRepository, times(1)).save(mediaCaptor.capture());
    WorkoutMediaEntity savedMedia = mediaCaptor.getValue();
    assertEquals("IMAGE", savedMedia.getMediaType().name());

    List<String> capturedBuckets = bucketCaptor.getAllValues();
    List<String> capturedKeys = keyCaptor.getAllValues();

    assertEquals(bucket, capturedBuckets.get(0));
    assertEquals(bucket, capturedBuckets.get(1));

    assertTrue(capturedKeys.get(0).matches("[0-9a-fA-F-]{36}"));
    assertTrue(capturedKeys.get(1).startsWith("thumb_"));

    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor = ArgumentCaptor.forClass(
        WorkoutSessionEntity.class);
    verify(workoutSessionRepository, times(1)).save(sessionCaptor.capture());
    WorkoutSessionEntity savedSession = sessionCaptor.getValue();
    assertEquals(1, savedSession.getWorkoutMedia().size());

    assertEquals(10000L, response.getSessionId());
    assertEquals(1, response.getOriginalUrls().size());
    assertEquals(1, response.getThumbnailUrls().size());
  }

  @Test
  @DisplayName("이미지 업로드 실패 - 이미지 개수가 10개를 초과하면 예외 발생")
  void testUploadWorkoutImageFailExcessiveImages() throws IOException {
    workoutSession.getWorkoutMedia()
        .addAll(Collections.nCopies(10, WorkoutMediaEntity.builder().mediaType(IMAGE).build()));

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(workoutSessionRepository.findByPtContract_TrainerAndId(trainer, 10000L))
        .thenReturn(Optional.of(workoutSession));

    byte[] imageBytes = Files.readAllBytes(
        Paths.get(System.getProperty("user.home") + "/Downloads/test.jpeg"));
    MockMultipartFile mockFile = new MockMultipartFile(
        "file", "test.jpg", "image/jpeg", imageBytes);

    WorkoutImageRequestDto dto = WorkoutImageRequestDto.builder()
        .sessionId(10000L)
        .images(List.of(mockFile))
        .build();

    assertThrows(MediaCountExceededException.class,
        () -> workoutSessionService.uploadWorkoutImage(dto));

    ArgumentCaptor<WorkoutMediaEntity> mediaCaptor = ArgumentCaptor.forClass(
        WorkoutMediaEntity.class);
    ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor = ArgumentCaptor.forClass(
        WorkoutSessionEntity.class);

    verify(workoutMediaRepository, never()).save(mediaCaptor.capture());
    verify(s3Operations, never()).upload(
        bucketCaptor.capture(), keyCaptor.capture(), inputStreamCaptor.capture(),
        metadataCaptor.capture());
    verify(workoutSessionRepository, never()).save(sessionCaptor.capture());

    assertTrue(mediaCaptor.getAllValues().isEmpty());
    assertTrue(bucketCaptor.getAllValues().isEmpty());
    assertTrue(keyCaptor.getAllValues().isEmpty());
    assertTrue(inputStreamCaptor.getAllValues().isEmpty());
    assertTrue(metadataCaptor.getAllValues().isEmpty());
    assertTrue(sessionCaptor.getAllValues().isEmpty());
  }

  @Test
  @DisplayName("이미지 업로드 실패 - 파일 타입이 맞지 않으면 예외 발생")
  void testUploadWorkoutImageFailInvalidFileType() {
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(workoutSessionRepository.findByPtContract_TrainerAndId(trainer, 10000L))
        .thenReturn(Optional.of(workoutSession));

    MockMultipartFile mockFile = new MockMultipartFile(
        "file", "test.txt", "text/plain", "test text content".getBytes());

    WorkoutImageRequestDto dto = WorkoutImageRequestDto.builder()
        .sessionId(10000L)
        .images(List.of(mockFile))
        .build();

    assertThrows(InvalidFileTypeException.class,
        () -> workoutSessionService.uploadWorkoutImage(dto));

    ArgumentCaptor<WorkoutMediaEntity> mediaCaptor = ArgumentCaptor.forClass(
        WorkoutMediaEntity.class);
    ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor = ArgumentCaptor.forClass(
        WorkoutSessionEntity.class);

    verify(workoutMediaRepository, never()).save(mediaCaptor.capture());
    verify(s3Operations, never()).upload(
        bucketCaptor.capture(), keyCaptor.capture(), inputStreamCaptor.capture(),
        metadataCaptor.capture());
    verify(workoutSessionRepository, never()).save(sessionCaptor.capture());

    assertTrue(mediaCaptor.getAllValues().isEmpty());
    assertTrue(bucketCaptor.getAllValues().isEmpty());
    assertTrue(keyCaptor.getAllValues().isEmpty());
    assertTrue(inputStreamCaptor.getAllValues().isEmpty());
    assertTrue(metadataCaptor.getAllValues().isEmpty());
    assertTrue(sessionCaptor.getAllValues().isEmpty());
  }

}