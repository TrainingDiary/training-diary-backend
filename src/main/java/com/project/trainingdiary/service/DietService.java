package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.CreateDietRequestDto;
import com.project.trainingdiary.dto.response.DietImageResponseDto;
import com.project.trainingdiary.entity.DietEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.InvalidFileTypeException;
import com.project.trainingdiary.exception.impl.PtContractNotExistException;
import com.project.trainingdiary.exception.impl.TraineeNotExistException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.repository.DietRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.util.ImageUtil;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
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
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userEmail = authentication.getName();

    TraineeEntity trainee = traineeRepository.findByEmail(userEmail).orElse(null);
    TrainerEntity trainer = trainerRepository.findByEmail(userEmail).orElse(null);

    if (trainee != null && trainee.getId().equals(id)) {
      return getDietsForTrainee(trainee, pageable);
    } else if (trainer != null) {
      TraineeEntity traineeToView = getTraineeById(id);
      if (hasContractWithTrainee(trainer, traineeToView)) {
        return getDietsForTrainee(traineeToView, pageable);
      } else {
        throw new PtContractNotExistException();
      }
    } else {
      throw new UserNotFoundException();
    }
  }

  /**
   * 트레이니의 식단 목록을 조회합니다.
   *
   * @param trainee  트레이니 엔티티
   * @param pageable 페이지 요청 정보
   * @return 트레이니의 식단 페이지
   */
  private Page<DietImageResponseDto> getDietsForTrainee(TraineeEntity trainee, Pageable pageable) {
    Page<DietEntity> dietPage = dietRepository.findByTraineeId(trainee.getId(), pageable);

    return dietPage.map(diet -> DietImageResponseDto.builder()
        .dietId(diet.getId())
        .thumbnailUrl(diet.getThumbnailUrl())
        .build());
  }

  /**
   * 트레이너가 트레이니와 계약을 맺고 있는지 확인합니다.
   *
   * @param trainer 트레이너 엔티티
   * @param trainee 트레이니 엔티티
   * @return 계약 여부
   */
  private boolean hasContractWithTrainee(TrainerEntity trainer, TraineeEntity trainee) {
    return ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), trainee.getId())
        .isPresent();
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
}