package com.project.trainingdiary.service;

import static com.project.trainingdiary.model.WorkoutMediaType.IMAGE;

import com.project.trainingdiary.dto.request.WorkoutMediaRequestDto;
import com.project.trainingdiary.dto.request.WorkoutSessionCreateRequestDto;
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

  private TrainerEntity getTrainer() {
    return trainerRepository
        .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
        .orElseThrow(UserNotFoundException::new);
  }

  /**
   * 운동 일지 목록 조회
   */
  public Page<WorkoutSessionListResponseDto> getWorkoutSessions(Long traineeId, Pageable pageable) {
    return workoutSessionRepository
        .findByPtContract_Trainee_IdOrderBySessionDateDesc(traineeId, pageable)
        .map(WorkoutSessionListResponseDto::fromEntity);
  }

  // TODO
  //  상세 조회 시에 key 가져와서 url 반환하는 로직 추가
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
  public void uploadWorkoutMedia(WorkoutMediaRequestDto dto) throws IOException {
    TrainerEntity trainer = getTrainer();

    // 이미지를 업로드할 운동 일지가 존재하는지 확인
    WorkoutSessionEntity workoutSession = workoutSessionRepository.findByPtContract_TrainerAndId(trainer, dto.getSessionId())
        .orElseThrow(() -> new WorkoutSessionNotFoundException(dto.getSessionId()));

    int existingImageCount = (int) workoutSession.getWorkoutMedia().stream()
        .filter(media -> media.getMediaType() == IMAGE).count();
    int newImageCount = dto.getMediaFiles().size();

    // 현재 운동 일지에 존재하는 이미지와 새로 받은 이미지의 합이 10을 넘으면 예외 발생 - 이미지는 최대 10개까지 업로드 가능
    if (existingImageCount + newImageCount > MAX_MEDIA_COUNT) {
      throw new MediaCountExceededException();
    }

    // 이미지 확인
    for (MultipartFile file : dto.getMediaFiles()) {
      if (!isValidImageType(file)) {
        throw new InvalidFileTypeException();
      }

      // key (이미지 이름) 설정 후 업로드
      String originalKey = UUID.randomUUID().toString();
      try (InputStream inputStream = file.getInputStream()){
        s3Operations.upload(bucket, originalKey, inputStream,
            ObjectMetadata.builder().contentType(file.getContentType()).build());
      }

      // 썸네일 생성 후 업로드
      String thumbnailKey = createAndUploadThumbnail(file, originalKey);

      WorkoutMediaEntity workoutMedia = WorkoutMediaEntity.builder()
          .originalKey(originalKey).thumbnailKey(thumbnailKey).mediaType(IMAGE).build();

      workoutMediaRepository.save(workoutMedia);
      workoutSession.getWorkoutMedia().add(workoutMedia);
    }
    workoutSessionRepository.save(workoutSession);
  }

  private String createAndUploadThumbnail(MultipartFile file, String originalKey) throws IOException {
    BufferedImage originalImage = ImageIO.read(file.getInputStream());
    BufferedImage thumbnailImage = Scalr.resize(originalImage, Method.QUALITY, Mode.AUTOMATIC, 150, 150);

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      ImageIO.write(thumbnailImage, "jpg", byteArrayOutputStream);
      try (InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
        String thumbnailKey = "thumb_" + originalKey;
        s3Operations.upload(bucket, thumbnailKey, inputStream, ObjectMetadata.builder().contentType(MediaType.IMAGE_JPEG_VALUE).build());
        return thumbnailKey;
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



}
