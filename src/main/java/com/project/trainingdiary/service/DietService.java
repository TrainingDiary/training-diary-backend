package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.diet.CreateDietRequestDto;
import com.project.trainingdiary.dto.response.diet.DietDetailsInfoResponseDto;
import com.project.trainingdiary.dto.response.diet.DietImageResponseDto;
import com.project.trainingdiary.entity.DietEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.diet.DietNotExistException;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.user.TraineeNotExistException;
import com.project.trainingdiary.exception.user.TrainerNotFoundException;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.exception.workout.InvalidFileTypeException;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.DietRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.util.ImageUtil;
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
  private final ImageUtil imageUtil;

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

    if (!imageUtil.isValidImageType(imageFile)) {
      throw new InvalidFileTypeException();
    }

    String originalUrl = imageUtil.uploadImageToS3(imageFile);
    String extension = imageUtil.getExtension(imageFile.getOriginalFilename());
    String thumbnailUrl = imageUtil.createAndUploadThumbnail(imageFile, originalUrl, extension);

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
   * @throws PtContractNotExistException 트레이너와 트레이니 사이에 계약이 없을 경우 예외 발생
   * @throws UserNotFoundException       인증된 사용자가 트레이니 또는 트레이너가 아닐 경우 예외 발생
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
    hasContractWithTrainee(trainer, trainee);

    Page<DietEntity> dietPage = dietRepository.findByTraineeId(id, pageable);

    return dietPage.map(diet -> DietImageResponseDto.builder()
        .dietId(diet.getId())
        .thumbnailUrl(diet.getThumbnailUrl())
        .build());
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

    Page<DietEntity> dietPage = dietRepository.findByTraineeId(id, pageable);

    return dietPage.map(diet -> DietImageResponseDto.builder()
        .dietId(diet.getId())
        .thumbnailUrl(diet.getThumbnailUrl())
        .build());
  }

  /**
   * 특정 식단의 상세 정보를 조회합니다.
   *
   * @param id 식단 ID
   * @return 식단 상세 정보 DTO
   * @throws DietNotExistException 식단이 존재하지 않을 경우 예외 발생
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

    DietEntity diet = dietRepository.findByTraineeIdAndId(trainee.getId(), id)
        .orElseThrow(DietNotExistException::new);

    return DietDetailsInfoResponseDto.of(diet);
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

    DietEntity diet = dietRepository.findById(id)
        .orElseThrow(DietNotExistException::new);

    hasContractWithTrainee(trainer, diet.getTrainee());

    return DietDetailsInfoResponseDto.of(diet);
  }

  /**
   * 트레이니의 식단을 삭제합니다.
   *
   * @param id 식단 ID
   * @throws DietNotExistException 식단이 존재하지 않을 위해 예외 발생
   */
  @Transactional
  public void deleteDiet(Long id) {

    TraineeEntity trainee = getAuthenticatedTrainee();

    DietEntity diet = dietRepository.findByTraineeIdAndId(trainee.getId(), id)
        .orElseThrow(DietNotExistException::new);

    imageUtil.deleteFileFromS3(diet.getOriginalUrl());
    imageUtil.deleteFileFromS3(diet.getThumbnailUrl());

    dietRepository.delete(diet);
  }

  /**
   * 트레이너가 트레이니와 계약을 맺고 있는지 확인합니다.
   *
   * @param trainer 트레이너 엔티티
   * @param trainee 트레이니 엔티티
   * @throws PtContractNotExistException 트레이너와 트레이니 사이에 계약이 없을 경우 예외 발생
   */
  private void hasContractWithTrainee(TrainerEntity trainer, TraineeEntity trainee) {
    ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), trainee.getId())
        .orElseThrow(PtContractNotExistException::new);
  }

  /**
   * 인증된 트레이니를 조회합니다.
   *
   * @return 인증된 트레이니 엔티티
   * @throws TraineeNotExistException 인증된 트레이니가 존재하지 않을 경우 예외 발생
   */
  private TraineeEntity getAuthenticatedTrainee() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getName() == null) {
      throw new TraineeNotExistException();
    }
    String email = authentication.getName();
    return traineeRepository.findByEmail(email)
        .orElseThrow(TraineeNotExistException::new);
  }

  /**
   * 인증된 트레이너를 조회합니다.
   *
   * @return 인증된 트레이너 엔티티
   * @throws TrainerNotFoundException 인증된 트레이너가 존재하지 않을 경우 예외 발생
   */
  private TrainerEntity getAuthenticatedTrainer() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getName() == null) {
      throw new TrainerNotFoundException();
    }
    String email = authentication.getName();
    return trainerRepository.findByEmail(email)
        .orElseThrow(TrainerNotFoundException::new);
  }

  /**
   * 트레이니 ID로 트레이니를 조회합니다.
   *
   * @param id 트레이니의 ID
   * @return 트레이니 엔티티
   * @throws TraineeNotExistException 트레이니가 존재하지 않을 경우 예외 발생
   */
  private TraineeEntity getTraineeById(Long id) {
    return traineeRepository.findById(id)
        .orElseThrow(TraineeNotExistException::new);
  }

  /**
   * 현재 인증된 사용자의 역할을 반환합니다.
   *
   * @return 인증된 사용자의 역할
   */
  private UserRoleType getMyRole() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRAINER"))) {
      return UserRoleType.TRAINER;
    } else {
      return UserRoleType.TRAINEE;
    }
  }
}