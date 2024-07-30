package com.project.trainingdiary.service;

import static com.project.trainingdiary.model.type.WorkoutMediaType.IMAGE;
import static com.project.trainingdiary.model.type.WorkoutMediaType.VIDEO;

import com.project.trainingdiary.dto.request.workout.session.WorkoutImageRequestDto;
import com.project.trainingdiary.dto.request.workout.session.WorkoutSessionCreateRequestDto;
import com.project.trainingdiary.dto.request.workout.session.WorkoutSessionUpdateRequestDto;
import com.project.trainingdiary.dto.request.workout.session.WorkoutVideoRequestDto;
import com.project.trainingdiary.dto.response.workout.session.WorkoutImageResponseDto;
import com.project.trainingdiary.dto.response.workout.session.WorkoutSessionListResponseDto;
import com.project.trainingdiary.dto.response.workout.session.WorkoutSessionResponseDto;
import com.project.trainingdiary.dto.response.workout.session.WorkoutVideoResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.entity.WorkoutEntity;
import com.project.trainingdiary.entity.WorkoutMediaEntity;
import com.project.trainingdiary.entity.WorkoutSessionEntity;
import com.project.trainingdiary.entity.WorkoutTypeEntity;
import com.project.trainingdiary.exception.ptcontract.PtContractNotFoundException;
import com.project.trainingdiary.exception.user.InvalidUserRoleTypeException;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.exception.workout.InvalidFileTypeException;
import com.project.trainingdiary.exception.workout.MediaCountExceededException;
import com.project.trainingdiary.exception.workout.WorkoutNotFoundException;
import com.project.trainingdiary.exception.workout.WorkoutSessionAccessDeniedException;
import com.project.trainingdiary.exception.workout.WorkoutSessionAlreadyExistException;
import com.project.trainingdiary.exception.workout.WorkoutSessionNotFoundException;
import com.project.trainingdiary.exception.workout.WorkoutTypeNotFoundException;
import com.project.trainingdiary.provider.S3ImageProvider;
import com.project.trainingdiary.provider.S3VideoProvider;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.WorkoutMediaRepository;
import com.project.trainingdiary.repository.WorkoutRepository;
import com.project.trainingdiary.repository.WorkoutSessionRepository;
import com.project.trainingdiary.repository.WorkoutTypeRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.util.MediaUtil;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class WorkoutSessionService {

  private final WorkoutTypeRepository workoutTypeRepository;
  private final WorkoutSessionRepository workoutSessionRepository;
  private final WorkoutRepository workoutRepository;
  private final WorkoutMediaRepository workoutMediaRepository;
  private final TrainerRepository trainerRepository;
  private final TraineeRepository traineeRepository;
  private final PtContractRepository ptContractRepository;

  private final S3ImageProvider s3ImageProvider;
  private final S3VideoProvider s3VideoProvider;

  private static final int MAX_IMAGE_COUNT = 10;
  private static final int MAX_VIDEO_COUNT = 5;
  private static final int THUMBNAIL_SIZE = 250;
  private static final int ORIGINAL_SIZE = 360;

  /**
   * 트레이너의 운동 일지 생성
   */
  public WorkoutSessionResponseDto createWorkoutSession(WorkoutSessionCreateRequestDto dto) {
    // 현재 로그인 되어있는 트레이너 본인의 엔티티
    TrainerEntity trainer = getTrainer();

    // 트레이너와 트레이니간의 PT 계약이 존재하는지 확인
    PtContractEntity ptContract = ptContractRepository
        .findByTrainerIdAndTraineeId(trainer.getId(), dto.getTraineeId())
        .orElseThrow(PtContractNotFoundException::new);

    // 세션 넘버에 대한 일지가 이미 존재하는지 확인
    workoutSessionRepository
        .findByPtContract_IdAndSessionNumber(ptContract.getId(), dto.getSessionNumber())
        .ifPresent(exist -> {
          throw new WorkoutSessionAlreadyExistException(dto.getSessionNumber());
        });

    // 운동 일지에서 운동 상세 내용(workout)은 운동 엔티티에 저장
    List<WorkoutEntity> workouts = dto.getWorkouts().stream().map(workoutDto -> {
      WorkoutTypeEntity workoutType = workoutTypeRepository.findById(workoutDto.getWorkoutTypeId())
          .orElseThrow(() -> new WorkoutTypeNotFoundException(workoutDto.getWorkoutTypeId()));
      WorkoutEntity workout = WorkoutEntity.toEntity(workoutDto, workoutType);
      workoutRepository.save(workout);
      return workout;
    }).toList();

    //  운동 일지 엔티티 저장
    WorkoutSessionEntity workoutSession = workoutSessionRepository
        .save(WorkoutSessionEntity.toEntity(dto, workouts, ptContract));

    return WorkoutSessionResponseDto.fromEntity(workoutSession);
  }

  /**
   * 트레이너의 운동 일지 수정
   */
  public WorkoutSessionResponseDto updateWorkoutSession(WorkoutSessionUpdateRequestDto dto) {
    // 현재 로그인 되어있는 트레이너 본인의 엔티티
    TrainerEntity trainer = getTrainer();

    // 일지가 존재하는지 확인
    WorkoutSessionEntity workoutSession = workoutSessionRepository
        .findByPtContract_TrainerAndId(trainer, dto.getSessionId())
        .orElseThrow(() -> new WorkoutSessionNotFoundException(dto.getSessionId()));

    List<WorkoutEntity> updateWorkouts = dto.getWorkouts().stream().map(workoutDto -> {
      WorkoutTypeEntity workoutType = workoutTypeRepository.findById(workoutDto.getWorkoutTypeId())
          .orElseThrow(() -> new WorkoutTypeNotFoundException(workoutDto.getWorkoutTypeId()));
      WorkoutEntity workout = workoutRepository.findById(workoutDto.getWorkoutId())
          .orElseThrow(() -> new WorkoutNotFoundException(workoutDto.getWorkoutId()));
      WorkoutEntity updateWorkout = WorkoutEntity.updateEntity(workoutDto, workoutType, workout);
      workoutRepository.save(updateWorkout);
      return updateWorkout;
    }).toList();

    workoutSession = workoutSessionRepository
        .save(WorkoutSessionEntity.updateEntity(dto, updateWorkouts, workoutSession));

    return WorkoutSessionResponseDto.fromEntity(workoutSession);
  }

  /**
   * 트레이너의 운동 일지 삭제
   */
  public void deleteWorkoutSession(Long workoutSessionId) {
    // 현재 로그인 되어있는 트레이너 본인의 엔티티
    TrainerEntity trainer = getTrainer();

    WorkoutSessionEntity workoutSession = workoutSessionRepository
        .findByPtContract_TrainerAndId(trainer, workoutSessionId)
        .orElseThrow(() -> new WorkoutSessionNotFoundException(workoutSessionId));

    List<WorkoutEntity> workouts = workoutSession.getWorkouts();
    workoutRepository.deleteAll(workouts);

    List<WorkoutMediaEntity> workoutMedias = workoutSession.getWorkoutMedia();
    for (WorkoutMediaEntity workoutMedia : workoutMedias) {
      s3ImageProvider.deleteMedia(workoutMedia.getOriginalUrl());
      if (workoutMedia.getThumbnailUrl() != null) {
        s3ImageProvider.deleteMedia(workoutMedia.getThumbnailUrl());
      }
    }
    workoutMediaRepository.deleteAll(workoutMedias);

    workoutSessionRepository.delete(workoutSession);
  }

  /**
   * 운동 일지 목록 조회
   */
  public Page<WorkoutSessionListResponseDto> getWorkoutSessions(Long traineeId, Pageable pageable) {
    return workoutSessionRepository
        .findByPtContract_Trainee_IdOrderBySessionDateDesc(traineeId, pageable)
        .map(WorkoutSessionListResponseDto::fromEntity);
  }

  /**
   * 운동 일지 상세 조회
   */
  public WorkoutSessionResponseDto getWorkoutSessionDetails(Long sessionId) {
    WorkoutSessionEntity workoutSession = workoutSessionRepository.findById(sessionId)
        .orElseThrow(() -> new WorkoutSessionNotFoundException(sessionId));

    String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator()
        .next().getAuthority();
    if ("ROLE_TRAINER".equals(role)) {
      TrainerEntity trainer = getTrainer();
      if (!workoutSession.getPtContract().getTrainer().equals(trainer)) {
        throw new WorkoutSessionAccessDeniedException();
      }
    } else if ("ROLE_TRAINEE".equals(role)) {
      TraineeEntity trainee = getTrainee();
      if (!workoutSession.getPtContract().getTrainee().equals(trainee)) {
        throw new WorkoutSessionAccessDeniedException();
      }
    } else {
      throw new InvalidUserRoleTypeException();
    }
    return WorkoutSessionResponseDto.fromEntity(workoutSession);
  }

  /**
   * 운동 일지 - 이미지 업로드
   */
  @Transactional
  public WorkoutImageResponseDto uploadWorkoutImage(WorkoutImageRequestDto dto) throws IOException {
    // 현재 로그인 되어있는 트레이너 본인의 엔티티
    TrainerEntity trainer = getTrainer();

    // 이미지를 업로드할 운동 일지가 존재하는지 확인
    WorkoutSessionEntity workoutSession = workoutSessionRepository
        .findByPtContract_TrainerAndId(trainer, dto.getSessionId())
        .orElseThrow(() -> new WorkoutSessionNotFoundException(dto.getSessionId()));

    int existingImageCount = (int) workoutSession.getWorkoutMedia().stream()
        .filter(media -> media.getMediaType().equals(IMAGE)).count();
    int newImageCount = dto.getImages().size();

    // 현재 운동 일지에 존재하는 이미지와 새로 받은 이미지의 합이 10을 넘으면 예외 발생 - 이미지는 최대 10개까지 업로드 가능
    if (existingImageCount + newImageCount > MAX_IMAGE_COUNT) {
      throw new MediaCountExceededException();
    }

    // 이미지 업로드 및 썸네일 생성
    for (MultipartFile file : dto.getImages()) {
      String extension = MediaUtil.getExtension(MediaUtil.checkFileNameExist(file));
      String uuid = UUID.randomUUID().toString();
      String originalKey = "original_" + uuid + "." + extension;
      String thumbnailKey = "thumb_" + uuid + "." + extension;

      String originalUrl = s3ImageProvider.uploadImage(file, originalKey, extension, ORIGINAL_SIZE);
      String thumbnailUrl = s3ImageProvider.uploadImage(file, thumbnailKey, extension,
          THUMBNAIL_SIZE);
      WorkoutMediaEntity workoutMedia = WorkoutMediaEntity.builder()
          .originalUrl(originalUrl)
          .thumbnailUrl(thumbnailUrl)
          .mediaType(IMAGE)
          .build();
      workoutMediaRepository.save(workoutMedia);
      workoutSession.getWorkoutMedia().add(workoutMedia);
    }
    workoutSessionRepository.save(workoutSession);

    List<WorkoutMediaEntity> imageUrlList = workoutSession.getWorkoutMedia().stream()
        .filter(media -> media.getMediaType().equals(IMAGE)).collect(Collectors.toList());

    return WorkoutImageResponseDto.fromEntity(imageUrlList, dto.getSessionId());
  }

  /**
   * 운동 일지 - 동영상 업로드
   */
  @Transactional
  public WorkoutVideoResponseDto uploadWorkoutVideo(WorkoutVideoRequestDto dto)
      throws IOException, InterruptedException {
    // 현재 로그인 되어있는 트레이너 본인의 엔티티
    TrainerEntity trainer = getTrainer();
    MultipartFile video = dto.getVideo();

    // 동영상을 업로드할 운동 일지가 존재하는지 확인
    WorkoutSessionEntity workoutSession = workoutSessionRepository
        .findByPtContract_TrainerAndId(trainer, dto.getSessionId())
        .orElseThrow(() -> new WorkoutSessionNotFoundException(dto.getSessionId()));

    int existingVideoCount = (int) workoutSession.getWorkoutMedia().stream()
        .filter(media -> media.getMediaType() == VIDEO).count();

    // 운동 일지에 이미 동영상이 5개 존재한다면 더이상 업로드 할 수 없음
    if (existingVideoCount >= MAX_VIDEO_COUNT) {
      throw new MediaCountExceededException();
    }

    // 동영상 타입 확인
    if (!MediaUtil.isValidVideoType(video)) {
      throw new InvalidFileTypeException();
    }

    String uuid = UUID.randomUUID().toString();
    String originalUrl = s3VideoProvider.uploadVideo(video, uuid);
    String thumbnailUrl = s3VideoProvider.uploadThumbnail(video, uuid);

    WorkoutMediaEntity workoutMedia = WorkoutMediaEntity.builder()
        .originalUrl(originalUrl).thumbnailUrl(thumbnailUrl).mediaType(VIDEO).build();
    workoutMediaRepository.save(workoutMedia);
    workoutSession.getWorkoutMedia().add(workoutMedia);
    workoutSessionRepository.save(workoutSession);

    List<WorkoutMediaEntity> workoutMedias = workoutSession.getWorkoutMedia().stream()
        .filter(media -> media.getMediaType() == VIDEO).toList();

    return WorkoutVideoResponseDto.fromEntity(workoutMedias, dto.getSessionId());
  }

  /**
   * 로그인한 트레이너 엔티티
   */
  private TrainerEntity getTrainer() {
    return trainerRepository
        .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
        .orElseThrow(UserNotFoundException::new);
  }

  /**
   * 로그인한 트레이니 엔티티
   */
  private TraineeEntity getTrainee() {
    return traineeRepository
        .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
        .orElseThrow(UserNotFoundException::new);
  }

}
