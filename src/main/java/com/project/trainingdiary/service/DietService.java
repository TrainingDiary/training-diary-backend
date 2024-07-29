package com.project.trainingdiary.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.project.trainingdiary.dto.request.diet.CreateDietRequestDto;
import com.project.trainingdiary.dto.response.diet.DietDetailsInfoResponseDto;
import com.project.trainingdiary.dto.response.diet.DietImageResponseDto;
import com.project.trainingdiary.entity.DietEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.diet.DietNotExistException;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.user.TraineeNotFoundException;
import com.project.trainingdiary.exception.user.TrainerNotFoundException;
import com.project.trainingdiary.exception.workout.InvalidFileTypeException;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.provider.S3DietImageProvider;
import com.project.trainingdiary.repository.DietRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.util.ConvertCloudFrontUrlUtil;
import com.project.trainingdiary.util.MediaUtil;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietService {

  private final DietRepository dietRepository;
  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;
  private final PtContractRepository ptContractRepository;

  private final S3DietImageProvider s3DietImageProvider;

  private final Cache<String, UserPrincipal> userCache;

  /**
   * 새로운 식단을 생성합니다.
   *
   * @param dto 식단 생성 요청 DTO
   * @throws IOException              이미지 업로드 중 오류 발생 시 예외 발생
   * @throws InvalidFileTypeException 이미지 파일 타입이 유효하지 않을 경우 예외 발생
   */
  @Transactional
  public void createDiet(CreateDietRequestDto dto) throws IOException {
    TraineeEntity trainee = getAuthenticatedTrainee();
    MultipartFile imageFile = dto.getImage();
    validateImageFileType(imageFile);

    String originalUrl = s3DietImageProvider.uploadImageToS3(imageFile);
    String extension = MediaUtil.getExtension(MediaUtil.checkFileNameExist(imageFile));
    String thumbnailUrl = s3DietImageProvider.uploadThumbnailToS3(imageFile, originalUrl,
        extension);

    DietEntity diet = new DietEntity();
    diet.setTrainee(trainee);
    diet.setContent(dto.getContent());
    diet.setOriginalUrl(originalUrl);
    diet.setThumbnailUrl(thumbnailUrl);

    dietRepository.save(diet);
  }

  /**
   * 트레이니 또는 트레이너가 식단 목록을 조회합니다.
   *
   * @param id       조회할 트레이니의 ID
   * @param pageable 페이지 요청 정보
   * @return 트레이니의 식단 페이지
   */
  public Page<DietImageResponseDto> getDiets(Long id, Pageable pageable) {
    UserRoleType role = getMyRole();

    if (role.equals(UserRoleType.TRAINEE)) {
      return getDietsForTrainee(id, pageable);
    } else {
      return getDietsForTraineeByTrainer(id, pageable);
    }
  }

  /**
   * 트레이너가 특정 트레이니의 식단 목록을 조회합니다.
   *
   * @param id       조회할 트레이니의 ID
   * @param pageable 페이지 요청 정보
   * @return 트레이니의 식단 페이지
   */
  private Page<DietImageResponseDto> getDietsForTraineeByTrainer(Long id, Pageable pageable) {
    TrainerEntity trainer = getAuthenticatedTrainer();
    TraineeEntity trainee = getTraineeById(id);
    validateContractWithTrainee(trainer, trainee);

    Page<DietEntity> page = dietRepository.findDietImagesByTraineeId(id, pageable);
    return mapToDietImageResponseDtos(page);
  }

  /**
   * 트레이니의 식단 목록을 조회합니다.
   *
   * @param id       트레이니의 ID
   * @param pageable 페이지 요청 정보
   * @return 트레이니의 식단 페이지
   */
  private Page<DietImageResponseDto> getDietsForTrainee(Long id, Pageable pageable) {
    TraineeEntity trainee = getAuthenticatedTrainee();

    if (!trainee.getId().equals(id)) {
      throw new DietNotExistException();
    }

    Page<DietEntity> page = dietRepository.findDietImagesByTraineeId(id, pageable);
    return mapToDietImageResponseDtos(page);
  }

  /**
   * 특정 식단의 상세 정보를 조회합니다.
   *
   * @param id 식단 ID
   * @return 식단 상세 정보 DTO
   */
  public DietDetailsInfoResponseDto getDietDetails(Long id) {
    UserRoleType role = getMyRole();

    if (role.equals(UserRoleType.TRAINEE)) {
      return getDietDetailsInfoForTrainee(id);
    } else {
      return getDietDetailsInfoForTrainer(id);
    }
  }

  /**
   * 트레이니가 자신의 특정 식단의 상세 정보를 조회합니다.
   *
   * @param id 식단 ID
   * @return 식단 상세 정보 DTO
   * @throws DietNotExistException 식단이 존재하지 않을 경우 예외 발생
   */
  private DietDetailsInfoResponseDto getDietDetailsInfoForTrainee(Long id) {
    TraineeEntity trainee = getAuthenticatedTrainee();

    DietEntity diet = dietRepository.findByTraineeIdAndIdWithCommentsAndTrainer(trainee.getId(), id)
        .orElseThrow(DietNotExistException::new);

    return DietDetailsInfoResponseDto.of(diet, diet.getComments());
  }

  /**
   * 트레이너가 특정 트레이니의 식단 상세 정보를 조회합니다.
   *
   * @param id 식단 ID
   * @return 식단 상세 정보 DTO
   * @throws DietNotExistException 식단이 존재하지 않을 경우 예외 발생
   */
  private DietDetailsInfoResponseDto getDietDetailsInfoForTrainer(Long id) {
    TrainerEntity trainer = getAuthenticatedTrainer();

    DietEntity diet = dietRepository.findByIdWithCommentsAndTrainer(id)
        .orElseThrow(DietNotExistException::new);

    validateContractWithTrainee(trainer, diet.getTrainee());

    return DietDetailsInfoResponseDto.of(diet, diet.getComments());
  }

  /**
   * 트레이니의 식단을 삭제합니다.
   *
   * @param id 식단 ID
   * @throws DietNotExistException 식단이 존재하지 않을 경우 예외 발생
   */
  @Transactional
  public void deleteDiet(Long id) {
    TraineeEntity trainee = getAuthenticatedTrainee();

    DietEntity diet = dietRepository.findByTraineeIdAndId(trainee.getId(), id)
        .orElseThrow(DietNotExistException::new);

    deleteDietImages(diet);

    dietRepository.delete(diet);
  }

  /**
   * 트레이너가 트레이니와 계약을 맺고 있는지 확인합니다.
   *
   * @param trainer 트레이너 엔티티
   * @param trainee 트레이니 엔티티
   * @throws PtContractNotExistException 트레이너와 트레이니 사이에 계약이 없을 경우 예외 발생
   */
  private void validateContractWithTrainee(TrainerEntity trainer, TraineeEntity trainee) {
    ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), trainee.getId())
        .orElseThrow(PtContractNotExistException::new);
  }

  /**
   * 인증된 트레이니를 조회합니다.
   *
   * @return 인증된 트레이니 엔티티
   * @throws TraineeNotFoundException 인증된 트레이니가 존재하지 않을 경우 예외 발생
   */
  private TraineeEntity getAuthenticatedTrainee() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
      throw new TraineeNotFoundException();
    }

    UserPrincipal cachedUser = userCache.getIfPresent(userPrincipal.getEmail());
    if (cachedUser != null && cachedUser.getTrainee() != null) {
      return cachedUser.getTrainee();
    }

    return traineeRepository.findByEmail(userPrincipal.getEmail())
        .orElseThrow(TraineeNotFoundException::new);
  }

  /**
   * 인증된 트레이너를 조회합니다.
   *
   * @return 인증된 트레이너 엔티티
   * @throws TrainerNotFoundException 인증된 트레이너가 존재하지 않을 경우 예외 발생
   */
  private TrainerEntity getAuthenticatedTrainer() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
      throw new TrainerNotFoundException();
    }

    UserPrincipal cachedUser = userCache.getIfPresent(userPrincipal.getEmail());
    if (cachedUser != null && cachedUser.getTrainer() != null) {
      return cachedUser.getTrainer();
    }

    return trainerRepository.findByEmail(userPrincipal.getEmail())
        .orElseThrow(TrainerNotFoundException::new);
  }

  /**
   * 트레이니 ID로 트레이니를 조회합니다.
   *
   * @param id 트레이니의 ID
   * @return 트레이니 엔티티
   * @throws TraineeNotFoundException 트레이니가 존재하지 않을 경우 예외 발생
   */
  private TraineeEntity getTraineeById(Long id) {
    return traineeRepository.findById(id)
        .orElseThrow(TraineeNotFoundException::new);
  }

  /**
   * 현재 인증된 사용자의 역할을 반환합니다.
   *
   * @return 인증된 사용자의 역할
   */
  private UserRoleType getMyRole() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_TRAINER")) ? UserRoleType.TRAINER
        : UserRoleType.TRAINEE;
  }

  /**
   * 이미지 파일 타입을 확인합니다.
   *
   * @param imageFile 이미지 파일
   * @throws InvalidFileTypeException 이미지 파일 타입이 유효하지 않을 경우 예외 발생
   */
  private void validateImageFileType(MultipartFile imageFile) {
    if (!MediaUtil.isValidImageType(imageFile)) {
      throw new InvalidFileTypeException();
    }
  }

  /**
   * 다이어트 이미지를 삭제합니다.
   *
   * @param diet 다이어트 엔티티
   */
  private void deleteDietImages(DietEntity diet) {
    s3DietImageProvider.deleteFileFromS3(diet.getOriginalUrl());
    s3DietImageProvider.deleteFileFromS3(diet.getThumbnailUrl());
  }

  /**
   * 다이어트 엔티티 페이지를 다이어트 이미지 응답 DTO 페이지로 변환합니다.
   *
   * @param dietPage 다이어트 엔티티 페이지
   * @return 다이어트 이미지 응답 DTO 페이지
   */
  private Page<DietImageResponseDto> mapToDietImageResponseDtos(Page<DietEntity> dietPage) {
    return dietPage.map(diet -> DietImageResponseDto.builder()
        .dietId(diet.getId())
        .thumbnailUrl(ConvertCloudFrontUrlUtil.convertToCloudFrontUrl(diet.getThumbnailUrl()))
        .build());
  }
}