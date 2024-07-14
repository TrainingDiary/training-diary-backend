package com.project.trainingdiary.service;

import static com.project.trainingdiary.model.WorkoutMediaType.IMAGE;

import com.project.trainingdiary.dto.request.WorkoutImageRequestDto;
import com.project.trainingdiary.dto.request.WorkoutSessionCreateRequestDto;
import com.project.trainingdiary.dto.response.WorkoutImageResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionListResponseDto;
import com.project.trainingdiary.dto.response.WorkoutSessionResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class WorkoutSessionService {

  private final WorkoutTypeRepository workoutTypeRepository;
  private final WorkoutSessionRepository workoutSessionRepository;
  private final WorkoutRepository workoutRepository;
  private final WorkoutMediaRepository workoutMediaRepository;
  private final TrainerRepository trainerRepository;
  private final PtContractRepository ptContractRepository;

  private final S3Operations s3Operations;

  private static final int MAX_MEDIA_COUNT = 10;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucket;

  /**
   * 트레이너의 운동 일지 생성
   */
  public void createWorkoutSession(WorkoutSessionCreateRequestDto dto) {
    // 현재 로그인 되어있는 트레이너 본인의 엔티티
    TrainerEntity trainer = getTrainer();

    // 트레이너와 트레이니간의 PT 계약이 존재하는지 확인
    PtContractEntity ptContract = ptContractRepository
        .findByTrainerIdAndTraineeId(trainer.getId(), dto.getTraineeId())
        .orElseThrow(PtContractNotFoundException::new);

    // 운동 일지에서 운동 상세 내용(workout)은 운동 엔티티에 저장
    List<WorkoutEntity> workouts = dto.getWorkouts().stream().map(details -> {
      WorkoutTypeEntity workoutType = workoutTypeRepository.findById(details.getWorkoutTypeId())
          .orElseThrow(() -> new WorkoutTypeNotFoundException(details.getWorkoutTypeId()));
      WorkoutEntity workout = WorkoutEntity.toEntity(details, workoutType);
      workoutRepository.save(workout);
      return workout;
    }).toList();

    //  운동 일지 엔티티 저장
    workoutSessionRepository.save(WorkoutSessionEntity.toEntity(dto, workouts, ptContract));
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
  public WorkoutSessionResponseDto getWorkoutSessionDetails(Long traineeId, Long sessionId) {
    WorkoutSessionEntity session = workoutSessionRepository
        .findByIdAndPtContract_Trainee_Id(sessionId, traineeId)
        .orElseThrow(() -> new WorkoutSessionNotFoundException(sessionId));
    return WorkoutSessionResponseDto.fromEntity(session);
  }

  /**
   * 운동 일지 - 이미지 업로드
   */
  public WorkoutImageResponseDto uploadWorkoutImage(WorkoutImageRequestDto dto) throws IOException {
    TrainerEntity trainer = getTrainer();

    // 이미지를 업로드할 운동 일지가 존재하는지 확인
    WorkoutSessionEntity workoutSession = workoutSessionRepository
        .findByPtContract_TrainerAndId(trainer, dto.getSessionId())
        .orElseThrow(() -> new WorkoutSessionNotFoundException(dto.getSessionId()));

    int existingImageCount = (int) workoutSession.getWorkoutMedia().stream()
        .filter(media -> media.getMediaType() == IMAGE).count();
    int newImageCount = dto.getImages().size();

    // 현재 운동 일지에 존재하는 이미지와 새로 받은 이미지의 합이 10을 넘으면 예외 발생 - 이미지는 최대 10개까지 업로드 가능
    if (existingImageCount + newImageCount > MAX_MEDIA_COUNT) {
      throw new MediaCountExceededException();
    }

    // 이미지 타입 확인
    for (MultipartFile file : dto.getImages()) {
      if (!isValidImageType(file)) {
        throw new InvalidFileTypeException();
      }

      // 확장자 추출
      String extension = getExtension(file.getOriginalFilename());
      // key (이미지 이름) 설정 후 업로드
      String originalKey = UUID.randomUUID() + "." + extension;
      S3Resource s3Resource;
      try (InputStream inputStream = file.getInputStream()) {
        s3Resource = s3Operations.upload(bucket, originalKey, inputStream,
            ObjectMetadata.builder().contentType(file.getContentType()).build());
      }
      String originalUrl = s3Resource.getURL().toExternalForm();

      // 썸네일 생성 후 업로드
      String thumbnailUrl = createAndUploadThumbnail(file, originalKey);

      WorkoutMediaEntity workoutMedia = WorkoutMediaEntity.builder()
          .originalUrl(originalUrl).thumbnailUrl(thumbnailUrl).mediaType(IMAGE).build();

      workoutMediaRepository.save(workoutMedia);
      workoutSession.getWorkoutMedia().add(workoutMedia);
    }
    workoutSessionRepository.save(workoutSession);

    List<WorkoutMediaEntity> imageUrlList = workoutSession.getWorkoutMedia().stream()
        .filter(media -> media.getMediaType() == IMAGE).toList();

    return WorkoutImageResponseDto.fromEntity(imageUrlList, dto.getSessionId());
  }

  /**
   * 썸네일 이미지 생성 및 업로드
   */
  private String createAndUploadThumbnail(MultipartFile file, String originalKey)
      throws IOException {
    BufferedImage originalImage = ImageIO.read(file.getInputStream());
    BufferedImage thumbnailImage = Scalr
        .resize(originalImage, Method.QUALITY, Mode.AUTOMATIC, 150, 150);

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      ImageIO.write(thumbnailImage, "jpg", byteArrayOutputStream);
      try (InputStream inputStream = new ByteArrayInputStream(
          byteArrayOutputStream.toByteArray())) {
        String thumbnailKey = "thumb_" + originalKey;
        S3Resource s3Resource = s3Operations.upload(bucket, thumbnailKey, inputStream,
            ObjectMetadata.builder().contentType(MediaType.IMAGE_JPEG_VALUE).build());
        return s3Resource.getURL().toExternalForm();
      }
    }
  }

  /**
   * 이미지 확인 - jpeg, png 가능
   */
  private boolean isValidImageType(MultipartFile file) {
    return MediaType.IMAGE_JPEG.toString().equals(file.getContentType()) ||
        MediaType.IMAGE_PNG.toString().equals(file.getContentType());
  }

  /**
   * 확장자 추출
   */
  private String getExtension(String filename) {
    if (filename == null) {
      return "";
    }
    int dotIndex = filename.lastIndexOf('.');
    return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
  }

  /**
   * 로그인한 트레이너 엔티티
   */
  private TrainerEntity getTrainer() {
    return trainerRepository
        .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
        .orElseThrow(UserNotFoundException::new);
  }

}
