package com.project.trainingdiary.service;

import static com.project.trainingdiary.model.UserRoleType.TRAINEE;
import static com.project.trainingdiary.model.UserRoleType.TRAINER;
import static com.project.trainingdiary.model.WorkoutMediaType.IMAGE;
import static com.project.trainingdiary.model.WorkoutMediaType.VIDEO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.project.trainingdiary.dto.request.WorkoutVideoRequestDto;
import com.project.trainingdiary.dto.response.WorkoutImageResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionListResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionResponseDto;
import com.project.trainingdiary.dto.response.WorkoutVideoResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutEntity;
import com.project.trainingdiary.entity.WorkoutMediaEntity;
import com.project.trainingdiary.entity.WorkoutSessionEntity;
import com.project.trainingdiary.entity.WorkoutTypeEntity;
import com.project.trainingdiary.exception.impl.InvalidFileTypeException;
import com.project.trainingdiary.exception.impl.InvalidUserRoleTypeException;
import com.project.trainingdiary.exception.impl.MediaCountExceededException;
import com.project.trainingdiary.exception.impl.PtContractNotFoundException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.exception.impl.WorkoutSessionAccessDeniedException;
import com.project.trainingdiary.exception.impl.WorkoutSessionNotFoundException;
import com.project.trainingdiary.exception.impl.WorkoutTypeNotFoundException;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.WorkoutMediaRepository;
import com.project.trainingdiary.repository.WorkoutRepository;
import com.project.trainingdiary.repository.WorkoutSessionRepository;
import com.project.trainingdiary.repository.WorkoutTypeRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
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
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkoutSessionServiceTest {

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  @Mock
  private TrainerRepository trainerRepository;

  @Mock
  private TraineeRepository traineeRepository;

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

  @Mock
  private MultipartFile video;

  @InjectMocks
  private WorkoutSessionService workoutSessionService;

  private TrainerEntity trainer;
  private TraineeEntity trainee;
  private PtContractEntity ptContract;
  private WorkoutTypeEntity workoutType;
  private WorkoutSessionEntity workoutSession;
  private WorkoutDto workoutDto;
  private WorkoutSessionCreateRequestDto createRequestDto;

  @BeforeEach
  void init() {
    trainer = TrainerEntity.builder().id(1L).email("trainer@gmail.com").role(TRAINER).build();
    trainee = TraineeEntity.builder().id(10L).role(TRAINEE).build();
    ptContract = PtContractEntity.builder().id(100L).trainer(trainer).trainee(trainee).build();
    workoutType = WorkoutTypeEntity.builder().id(1000L).name("workout1").targetMuscle("target1")
        .remarks("remark1").trainer(trainer).build();
    workoutSession = WorkoutSessionEntity.builder().id(10000L).sessionDate(LocalDate.now())
        .sessionNumber(1).ptContract(ptContract).workouts(new ArrayList<>())
        .workoutMedia(new ArrayList<>()).build();

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(traineeRepository.findByEmail("trainee@gmail.com")).thenReturn(Optional.of(trainee));
  }

  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("운동 일지 생성 성공")
  void testCreateWorkoutSessionSuccess() {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    createRequestDto = WorkoutSessionCreateRequestDto.builder()
        .traineeId(trainee.getId())
        .sessionDate(LocalDate.now())
        .sessionNumber(1)
        .specialNote("specialNote1")
        .workouts(Collections.singletonList(
            WorkoutDto.builder()
                .workoutTypeId(workoutType.getId())
                .workoutTypeName(workoutType.getName())
                .targetMuscle(workoutType.getTargetMuscle())
                .remarks(workoutType.getRemarks())
                .build()))
        .build();

    WorkoutEntity workout = WorkoutEntity.builder()
        .workoutTypeName(workoutType.getName())
        .targetMuscle(workoutType.getTargetMuscle())
        .remarks(workoutType.getRemarks())
        .build();

    WorkoutSessionEntity newWorkoutSession = WorkoutSessionEntity.builder()
        .id(10001L)
        .sessionDate(createRequestDto.getSessionDate())
        .sessionNumber(createRequestDto.getSessionNumber())
        .specialNote(createRequestDto.getSpecialNote())
        .ptContract(ptContract)
        .workouts(Collections.singletonList(workout))
        .workoutMedia(new ArrayList<>())
        .build();

    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), trainee.getId()))
        .thenReturn(Optional.of(ptContract));
    when(workoutTypeRepository.findById(workoutType.getId()))
        .thenReturn(Optional.of(workoutType));

    ArgumentCaptor<WorkoutEntity> workoutCaptor = ArgumentCaptor.forClass(WorkoutEntity.class);
    when(workoutRepository.save(workoutCaptor.capture())).thenReturn(workout);

    ArgumentCaptor<WorkoutSessionEntity> workoutSessionCaptor = ArgumentCaptor
        .forClass(WorkoutSessionEntity.class);
    when(workoutSessionRepository.save(workoutSessionCaptor.capture()))
        .thenReturn(newWorkoutSession);

    WorkoutSessionResponseDto responseDto = workoutSessionService
        .createWorkoutSession(createRequestDto);

    assertNotNull(responseDto);
    assertEquals(newWorkoutSession.getId(), responseDto.getId());
    assertEquals(newWorkoutSession.getSessionDate(), responseDto.getSessionDate());
    assertEquals(newWorkoutSession.getSessionNumber(), responseDto.getSessionNumber());
    assertEquals(newWorkoutSession.getSpecialNote(), responseDto.getSpecialNote());
    assertEquals(newWorkoutSession.getWorkouts().size(), responseDto.getWorkouts().size());

    verify(ptContractRepository, times(1))
        .findByTrainerIdAndTraineeId(trainer.getId(), trainee.getId());
    verify(workoutTypeRepository, times(1)).findById(workoutType.getId());
    verify(workoutRepository, times(1)).save(workoutCaptor.capture());
    verify(workoutSessionRepository, times(1)).save(workoutSessionCaptor.capture());
  }

  @Test
  @DisplayName("운동 일지 생성 실패 - 트레이너를 찾지 못할 때 예외 발생")
  void testCreateWorkoutSessionFailTrainerNotFound() {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.empty());

    workoutDto = WorkoutDto.builder().workoutTypeId(workoutType.getId()).build();

    createRequestDto = WorkoutSessionCreateRequestDto.builder()
        .traineeId(trainer.getId()).workouts(List.of(workoutDto)).build();

    assertThrows(UserNotFoundException.class,
        () -> workoutSessionService.createWorkoutSession(createRequestDto));

    verify(trainerRepository, times(1)).findByEmail("trainer@gmail.com");
    verifyNoInteractions(ptContractRepository, workoutTypeRepository, workoutRepository,
        workoutSessionRepository);
  }

  @Test
  @DisplayName("운동 일지 생성 실패 - PT 계약이 존재 하지 않을 때 예외 발생")
  void testCreateWorkoutSessionFailPtContractNotFound() {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), trainee.getId()))
        .thenReturn(Optional.empty());

    workoutDto = WorkoutDto.builder().workoutTypeId(workoutType.getId()).build();

    createRequestDto = WorkoutSessionCreateRequestDto.builder()
        .traineeId(trainee.getId()).workouts(List.of(workoutDto)).build();

    assertThrows(PtContractNotFoundException.class,
        () -> workoutSessionService.createWorkoutSession(createRequestDto));

    verify(trainerRepository, times(1)).findByEmail("trainer@gmail.com");
    verify(ptContractRepository, times(1)).findByTrainerIdAndTraineeId(trainer.getId(),
        trainee.getId());
    verifyNoInteractions(workoutTypeRepository, workoutRepository, workoutSessionRepository);
  }

  @Test
  @DisplayName("운동 일지 생성 실패 - 운동 종류가 존재 하지 않을 때 예외 발생")
  void testCreateWorkoutSessionFailWorkoutTypeNotFound() {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), trainee.getId()))
        .thenReturn(Optional.of(ptContract));
    when(workoutTypeRepository.findById(workoutType.getId())).thenReturn(Optional.empty());

    workoutDto = WorkoutDto.builder().workoutTypeId(workoutType.getId()).build();

    createRequestDto = WorkoutSessionCreateRequestDto.builder()
        .traineeId(trainee.getId()).workouts(List.of(workoutDto)).build();

    assertThrows(WorkoutTypeNotFoundException.class,
        () -> workoutSessionService.createWorkoutSession(createRequestDto));

    verify(trainerRepository, times(1)).findByEmail("trainer@gmail.com");
    verify(ptContractRepository, times(1))
        .findByTrainerIdAndTraineeId(trainer.getId(), trainee.getId());
    verify(workoutTypeRepository, times(1)).findById(workoutType.getId());
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
  @DisplayName("운동 일지 상세 조회 성공 - 트레이너")
  void testGetWorkoutSessionDetailsForTrainerSuccess() {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(workoutSessionRepository.findById(workoutSession.getId())).thenReturn(
        Optional.of(workoutSession));
    WorkoutSessionResponseDto responseDto = workoutSessionService.getWorkoutSessionDetails(
        workoutSession.getId());

    assertNotNull(responseDto);
    assertEquals(workoutSession.getId(), responseDto.getId());
    assertEquals(workoutSession.getSessionDate(), responseDto.getSessionDate());
    assertEquals(workoutSession.getSessionNumber(), responseDto.getSessionNumber());

    verify(workoutSessionRepository, times(1)).findById(workoutSession.getId());
  }

  @Test
  @DisplayName("운동 일지 상세 조회 성공 - 트레이니")
  void testGetWorkoutSessionDetailsForTraineeSuccess() {
    Authentication authentication = new TestingAuthenticationToken("trainee@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINEE")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(workoutSessionRepository.findById(workoutSession.getId())).thenReturn(
        Optional.of(workoutSession));
    WorkoutSessionResponseDto response = workoutSessionService.getWorkoutSessionDetails(
        workoutSession.getId());

    assertNotNull(response);
    assertEquals(workoutSession.getId(), response.getId());
    assertEquals(workoutSession.getSessionDate(), response.getSessionDate());
    assertEquals(workoutSession.getSessionNumber(), response.getSessionNumber());

    verify(workoutSessionRepository, times(1)).findById(workoutSession.getId());
  }

  @Test
  @DisplayName("운동 일지 상세 조회 실패 - 트레이너 본인이 작성한 일지가 아닌 경우 예외 발생")
  void testGetWorkoutSessionDetailsAccessDeniedForTrainer() {
    TrainerEntity anotherTrainer = TrainerEntity.builder().id(2L).email("another_trainer@gmail.com")
        .role(TRAINER).build();
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(
        Optional.of(anotherTrainer));
    when(workoutSessionRepository.findById(workoutSession.getId())).thenReturn(
        Optional.of(workoutSession));

    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    assertThrows(WorkoutSessionAccessDeniedException.class,
        () -> workoutSessionService.getWorkoutSessionDetails(workoutSession.getId()));

    verify(workoutSessionRepository, times(1)).findById(workoutSession.getId());
  }

  @Test
  @DisplayName("운동 일지 상세 조회 실패 - 트레이니 본인의 일지가 아닌 경우 예외 발생")
  void testGetWorkoutSessionDetailsAccessDeniedForTrainee() {
    Authentication authentication = new TestingAuthenticationToken("trainee@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINEE")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    TraineeEntity anotherTrainee = TraineeEntity.builder().id(11L)
        .email("another_trainee@gmail.com").role(TRAINEE).build();
    when(traineeRepository.findByEmail("trainee@gmail.com")).thenReturn(
        Optional.of(anotherTrainee));
    when(workoutSessionRepository.findById(workoutSession.getId())).thenReturn(
        Optional.of(workoutSession));

    assertThrows(WorkoutSessionAccessDeniedException.class,
        () -> workoutSessionService.getWorkoutSessionDetails(workoutSession.getId()));

    verify(workoutSessionRepository, times(1)).findById(workoutSession.getId());
  }

  @Test
  @DisplayName("운동 일지 상세 조회 실패 - 트레이너와 트레이니가 아닌 다른 role 이용해 접근 시 예외 발생")
  void testGetWorkoutSessionDetailsFailureInvalidRole() {
    Authentication authentication = new TestingAuthenticationToken("unknown@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_UNKNOWN")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(workoutSessionRepository.findById(workoutSession.getId())).thenReturn(
        Optional.of(workoutSession));

    assertThrows(InvalidUserRoleTypeException.class,
        () -> workoutSessionService.getWorkoutSessionDetails(workoutSession.getId()));

    verify(workoutSessionRepository, times(1)).findById(workoutSession.getId());
  }

  @Test
  @DisplayName("이미지 업로드 성공")
  void testUploadWorkoutImageSuccess() throws IOException {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(workoutSessionRepository.findByPtContract_TrainerAndId(trainer, workoutSession.getId()))
        .thenReturn(Optional.of(workoutSession));

    BufferedImage img = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setColor(Color.RED);
    g2d.fillRect(0, 0, 500, 500);
    g2d.dispose();

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(img, "jpg", byteArrayOutputStream);
    byte[] imageBytes = byteArrayOutputStream.toByteArray();
    MockMultipartFile mockFile = new MockMultipartFile(
        "file", "test.jpg", "image/jpeg", imageBytes);

    WorkoutImageRequestDto imageRequestDto = WorkoutImageRequestDto.builder()
        .sessionId(workoutSession.getId())
        .images(List.of(mockFile))
        .build();

    S3Resource s3ResourceOriginal = mock(S3Resource.class);
    S3Resource s3ResourceThumbnail = mock(S3Resource.class);

    when(s3ResourceOriginal.getURL()).thenReturn(
        new URL("https://test-bucket.s3.amazonaws.com/original.jpg"));
    when(s3ResourceThumbnail.getURL()).thenReturn(
        new URL("https://test-bucket.s3.amazonaws.com/thumb_original.jpg"));

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

    WorkoutImageResponseDto response = workoutSessionService.uploadWorkoutImage(imageRequestDto);

    ArgumentCaptor<WorkoutMediaEntity> mediaCaptor = ArgumentCaptor
        .forClass(WorkoutMediaEntity.class);
    verify(workoutMediaRepository, times(1)).save(mediaCaptor.capture());
    WorkoutMediaEntity savedMedia = mediaCaptor.getValue();
    assertEquals("IMAGE", savedMedia.getMediaType().name());

    List<String> capturedBuckets = bucketCaptor.getAllValues();
    List<String> capturedKeys = keyCaptor.getAllValues();

    assertEquals(bucket, capturedBuckets.get(0));
    assertEquals(bucket, capturedBuckets.get(1));

    assertTrue(capturedKeys.get(0).matches("[0-9a-fA-F-]{36}\\.jpg"));
    assertTrue(capturedKeys.get(1).startsWith("thumb_"));
    assertTrue(capturedKeys.get(1).matches("thumb_[0-9a-fA-F-]{36}\\.jpg"));

    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor = ArgumentCaptor
        .forClass(WorkoutSessionEntity.class);
    verify(workoutSessionRepository, times(1)).save(sessionCaptor.capture());
    WorkoutSessionEntity savedSession = sessionCaptor.getValue();
    assertEquals(1, savedSession.getWorkoutMedia().size());

    assertEquals(workoutSession.getId(), response.getSessionId());
    assertEquals(1, response.getOriginalUrls().size());
    assertEquals(1, response.getThumbnailUrls().size());
  }

  @Test
  @DisplayName("이미지 업로드 실패 - 이미지 개수가 10개를 초과하면 예외 발생")
  void testUploadWorkoutImageFailExcessiveImages() throws IOException {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    workoutSession.getWorkoutMedia()
        .addAll(Collections.nCopies(10, WorkoutMediaEntity.builder().mediaType(IMAGE).build()));

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(workoutSessionRepository.findByPtContract_TrainerAndId(trainer, workoutSession.getId()))
        .thenReturn(Optional.of(workoutSession));

    BufferedImage img = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setColor(Color.RED);
    g2d.fillRect(0, 0, 500, 500);
    g2d.dispose();

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(img, "jpg", byteArrayOutputStream);
    byte[] imageBytes = byteArrayOutputStream.toByteArray();
    MockMultipartFile mockFile = new MockMultipartFile(
        "file", "test.jpg", "image/jpeg", imageBytes);

    WorkoutImageRequestDto imageRequestDto = WorkoutImageRequestDto.builder()
        .sessionId(workoutSession.getId())
        .images(List.of(mockFile))
        .build();

    assertThrows(MediaCountExceededException.class,
        () -> workoutSessionService.uploadWorkoutImage(imageRequestDto));

    ArgumentCaptor<WorkoutMediaEntity> mediaCaptor = ArgumentCaptor
        .forClass(WorkoutMediaEntity.class);
    ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor = ArgumentCaptor
        .forClass(WorkoutSessionEntity.class);

    verify(workoutMediaRepository, never()).save(mediaCaptor.capture());
    verify(s3Operations, never()).upload(bucketCaptor.capture(), keyCaptor.capture(),
        inputStreamCaptor.capture(), metadataCaptor.capture());
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
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(workoutSessionRepository.findByPtContract_TrainerAndId(trainer, workoutSession.getId()))
        .thenReturn(Optional.of(workoutSession));

    MockMultipartFile mockFile = new MockMultipartFile(
        "file", "test.txt", "text/plain", "test text content".getBytes());

    WorkoutImageRequestDto imageRequestDto = WorkoutImageRequestDto.builder()
        .sessionId(10000L)
        .images(List.of(mockFile))
        .build();

    assertThrows(InvalidFileTypeException.class,
        () -> workoutSessionService.uploadWorkoutImage(imageRequestDto));

    ArgumentCaptor<WorkoutMediaEntity> mediaCaptor = ArgumentCaptor
        .forClass(WorkoutMediaEntity.class);
    ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor = ArgumentCaptor
        .forClass(WorkoutSessionEntity.class);

    verify(workoutMediaRepository, never()).save(mediaCaptor.capture());
    verify(s3Operations, never()).upload(bucketCaptor.capture(), keyCaptor.capture(),
        inputStreamCaptor.capture(), metadataCaptor.capture());
    verify(workoutSessionRepository, never()).save(sessionCaptor.capture());

    assertTrue(mediaCaptor.getAllValues().isEmpty());
    assertTrue(bucketCaptor.getAllValues().isEmpty());
    assertTrue(keyCaptor.getAllValues().isEmpty());
    assertTrue(inputStreamCaptor.getAllValues().isEmpty());
    assertTrue(metadataCaptor.getAllValues().isEmpty());
    assertTrue(sessionCaptor.getAllValues().isEmpty());
  }

  @Test
  @DisplayName("동영상 업로드 성공")
  public void testUploadWorkoutVideoSuccess() throws IOException {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(workoutSessionRepository.findByPtContract_TrainerAndId(trainer, workoutSession.getId()))
        .thenReturn(Optional.of(workoutSession));
    when(video.getContentType()).thenReturn("video/mp4");
    InputStream inputStream = new ByteArrayInputStream("video".getBytes());
    when(video.getInputStream()).thenReturn(inputStream);

    WorkoutVideoRequestDto videoRequestDto = WorkoutVideoRequestDto.builder()
        .sessionId(workoutSession.getId())
        .video(video).build();

    S3Resource s3Resource = mock(S3Resource.class);
    when(s3Resource.getURL()).thenReturn(new URL("https://test-bucket.s3.amazonaws.com/original"));

    ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);

    doAnswer(invocation -> s3Resource).when(s3Operations)
        .upload(bucketCaptor.capture(), keyCaptor.capture(),
            inputStreamCaptor.capture(), metadataCaptor.capture());

    WorkoutVideoResponseDto response = workoutSessionService.uploadWorkoutVideo(videoRequestDto);

    ArgumentCaptor<WorkoutMediaEntity> mediaCaptor = ArgumentCaptor
        .forClass(WorkoutMediaEntity.class);
    verify(workoutMediaRepository, times(1)).save(mediaCaptor.capture());
    WorkoutMediaEntity savedMedia = mediaCaptor.getValue();
    assertEquals("VIDEO", savedMedia.getMediaType().name());

    List<String> capturedBuckets = bucketCaptor.getAllValues();
    List<String> capturedKeys = keyCaptor.getAllValues();

    assertEquals(bucket, capturedBuckets.get(0));
    assertFalse(capturedKeys.isEmpty());

    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor = ArgumentCaptor
        .forClass(WorkoutSessionEntity.class);
    verify(workoutSessionRepository, times(1)).save(sessionCaptor.capture());
    WorkoutSessionEntity savedSession = sessionCaptor.getValue();
    assertEquals(1, savedSession.getWorkoutMedia().size());

    assertEquals(workoutSession.getId(), response.getSessionId());
    assertEquals(1, response.getOriginalUrls().size());
  }

  @Test
  @DisplayName("동영상 업로드 실패 - 운동 일지를 찾을 수 없을 때 예외 발생")
  public void testUploadWorkoutVideoFailWorkoutSessionNotFound() {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(workoutSessionRepository.findByPtContract_TrainerAndId(trainer, workoutSession.getId()))
        .thenReturn(Optional.empty());

    WorkoutVideoRequestDto dto = WorkoutVideoRequestDto.builder()
        .sessionId(workoutSession.getId()).video(video).build();

    assertThrows(WorkoutSessionNotFoundException.class,
        () -> workoutSessionService.uploadWorkoutVideo(dto));

    ArgumentCaptor<WorkoutMediaEntity> mediaCaptor = ArgumentCaptor
        .forClass(WorkoutMediaEntity.class);
    ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor = ArgumentCaptor
        .forClass(WorkoutSessionEntity.class);

    verify(workoutMediaRepository, never()).save(mediaCaptor.capture());
    verify(s3Operations, never()).upload(bucketCaptor.capture(), keyCaptor.capture(),
        inputStreamCaptor.capture(), metadataCaptor.capture());
    verify(workoutSessionRepository, never()).save(sessionCaptor.capture());

    assertTrue(mediaCaptor.getAllValues().isEmpty());
    assertTrue(bucketCaptor.getAllValues().isEmpty());
    assertTrue(keyCaptor.getAllValues().isEmpty());
    assertTrue(inputStreamCaptor.getAllValues().isEmpty());
    assertTrue(metadataCaptor.getAllValues().isEmpty());
    assertTrue(sessionCaptor.getAllValues().isEmpty());
  }

  @Test
  @DisplayName("동영상 업로드 실패 - 동영상 개수가 10개를 초과하면 예외 발생")
  public void testUploadWorkoutVideoFailMediaCountExceeded() {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    workoutSession.getWorkoutMedia().addAll(
        Collections.nCopies(10, WorkoutMediaEntity.builder().mediaType(VIDEO).build()));
    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(workoutSessionRepository.findByPtContract_TrainerAndId(trainer, workoutSession.getId()))
        .thenReturn(Optional.of(workoutSession));

    WorkoutVideoRequestDto videoRequestDto = WorkoutVideoRequestDto.builder()
        .sessionId(workoutSession.getId()).video(video).build();

    assertThrows(MediaCountExceededException.class,
        () -> workoutSessionService.uploadWorkoutVideo(videoRequestDto));

    ArgumentCaptor<WorkoutMediaEntity> mediaCaptor = ArgumentCaptor
        .forClass(WorkoutMediaEntity.class);
    ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor = ArgumentCaptor
        .forClass(WorkoutSessionEntity.class);

    verify(workoutMediaRepository, never()).save(mediaCaptor.capture());
    verify(s3Operations, never()).upload(bucketCaptor.capture(), keyCaptor.capture(),
        inputStreamCaptor.capture(), metadataCaptor.capture());
    verify(workoutSessionRepository, never()).save(sessionCaptor.capture());

    assertTrue(mediaCaptor.getAllValues().isEmpty());
    assertTrue(bucketCaptor.getAllValues().isEmpty());
    assertTrue(keyCaptor.getAllValues().isEmpty());
    assertTrue(inputStreamCaptor.getAllValues().isEmpty());
    assertTrue(metadataCaptor.getAllValues().isEmpty());
    assertTrue(sessionCaptor.getAllValues().isEmpty());
  }

  @Test
  @DisplayName("동영상 업로드 실패 - 파일 타입이 맞지 않으면 예외 발생")
  public void testUploadWorkoutVideoFailInvalidFileType() {
    Authentication authentication = new TestingAuthenticationToken("trainer@gmail.com", null,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    when(trainerRepository.findByEmail("trainer@gmail.com")).thenReturn(Optional.of(trainer));
    when(workoutSessionRepository.findByPtContract_TrainerAndId(trainer, workoutSession.getId()))
        .thenReturn(Optional.of(workoutSession));
    when(video.getContentType()).thenReturn("video/avi");

    WorkoutVideoRequestDto dto = WorkoutVideoRequestDto.builder()
        .sessionId(workoutSession.getId()).video(video).build();

    assertThrows(InvalidFileTypeException.class,
        () -> workoutSessionService.uploadWorkoutVideo(dto));

    ArgumentCaptor<WorkoutMediaEntity> mediaCaptor = ArgumentCaptor
        .forClass(WorkoutMediaEntity.class);
    ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
    ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
    ArgumentCaptor<WorkoutSessionEntity> sessionCaptor = ArgumentCaptor
        .forClass(WorkoutSessionEntity.class);

    verify(workoutMediaRepository, never()).save(mediaCaptor.capture());
    verify(s3Operations, never()).upload(bucketCaptor.capture(), keyCaptor.capture(),
        inputStreamCaptor.capture(), metadataCaptor.capture());
    verify(workoutSessionRepository, never()).save(sessionCaptor.capture());

    assertTrue(mediaCaptor.getAllValues().isEmpty());
    assertTrue(bucketCaptor.getAllValues().isEmpty());
    assertTrue(keyCaptor.getAllValues().isEmpty());
    assertTrue(inputStreamCaptor.getAllValues().isEmpty());
    assertTrue(metadataCaptor.getAllValues().isEmpty());
    assertTrue(sessionCaptor.getAllValues().isEmpty());
  }

}