package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.trainer.AddInBodyInfoRequestDto;
import com.project.trainingdiary.dto.request.trainer.EditTraineeInfoRequestDto;
import com.project.trainingdiary.dto.response.trainer.AddInBodyInfoResponseDto;
import com.project.trainingdiary.dto.response.trainer.EditTraineeInfoResponseDto;
import com.project.trainingdiary.dto.response.trainer.TraineeInfoResponseDto;
import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.user.TraineeNotFoundException;
import com.project.trainingdiary.exception.user.TrainerNotFoundException;
import com.project.trainingdiary.repository.InBodyRecordHistoryRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainerService {

  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;
  private final InBodyRecordHistoryRepository inBodyRecordHistoryRepository;
  private final PtContractRepository ptContractRepository;

  /**
   * 트레이니 정보를 조회합니다.
   *
   * @param id 트레이니의 ID
   * @return TraineeInfoResponseDto 트레이니의 정보와 남은 세션 수를 포함한 응답 DTO
   */
  public TraineeInfoResponseDto getTraineeInfo(Long id) {
    TrainerEntity trainer = getAuthenticatedTrainer();
    TraineeEntity trainee = getTraineeById(id);
    PtContractEntity ptContract = getPtContract(trainer, trainee);

    return TraineeInfoResponseDto.fromEntity(trainee, ptContract.getRemainingSession());
  }

  /**
   * 새로운 인바디 기록을 추가합니다.
   *
   * @param dto 인바디 기록 추가 요청 DTO
   * @return AddInBodyInfoResponseDto 추가된 인바디 기록을 포함한 응답 DTO
   */
  public AddInBodyInfoResponseDto addInBodyRecord(AddInBodyInfoRequestDto dto) {
    TrainerEntity trainer = getAuthenticatedTrainer();
    TraineeEntity trainee = getTraineeById(dto.getTraineeId());

    validateContractExists(trainer, trainee);

    InBodyRecordHistoryEntity inBodyRecord = AddInBodyInfoRequestDto.toEntity(dto, trainee);
    inBodyRecordHistoryRepository.save(inBodyRecord);

    return AddInBodyInfoResponseDto.fromEntity(inBodyRecord);
  }

  /**
   * 트레이니 정보를 수정합니다.
   *
   * @param dto 트레이니 정보 수정 요청 DTO
   * @return EditTraineeInfoResponseDto 수정된 트레이니 정보를 포함한 응답 DTO
   */
  @Transactional
  public EditTraineeInfoResponseDto editTraineeInfo(EditTraineeInfoRequestDto dto) {
    TrainerEntity trainer = getAuthenticatedTrainer();
    TraineeEntity trainee = getTraineeById(dto.getTraineeId());
    PtContractEntity ptContract = getPtContract(trainer, trainee);

    updateTraineeInfo(trainee, dto);
    updateRemainingSession(ptContract, dto.getRemainingSession());

    return EditTraineeInfoResponseDto.fromEntity(trainee, ptContract);
  }

  /**
   * 트레이니 ID로 트레이니를 조회합니다.
   *
   * @param id 트레이니의 ID
   * @return 트레이니 엔티티 (존재할 경우)
   * @throws TraineeNotFoundException 트레이니가 존재하지 않을 경우 예외 발생
   */
  private TraineeEntity getTraineeById(Long id) {
    return traineeRepository.findById(id)
        .orElseThrow(TraineeNotFoundException::new);
  }

  /**
   * 인증된 트레이너를 조회합니다.
   *
   * @return 트레이너 엔티티
   * @throws TrainerNotFoundException 트레이너가 존재하지 않을 경우 예외 발생
   */
  private TrainerEntity getAuthenticatedTrainer() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getName() == null) {
      throw new TrainerNotFoundException();
    }
    return trainerRepository.findByEmail(authentication.getName())
        .orElseThrow(TrainerNotFoundException::new);
  }

  /**
   * 트레이너와 트레이니 간의 PT 계약을 조회합니다.
   *
   * @param trainer 트레이너 엔티티
   * @param trainee 트레이니 엔티티
   * @return PT 계약 엔티티
   * @throws PtContractNotExistException 계약이 존재하지 않을 경우 예외 발생
   */
  private PtContractEntity getPtContract(TrainerEntity trainer, TraineeEntity trainee) {
    return ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), trainee.getId())
        .orElseThrow(PtContractNotExistException::new);
  }

  /**
   * 트레이너와 트레이니 간의 계약 존재 여부를 확인합니다.
   *
   * @param trainer 트레이너 엔티티
   * @param trainee 트레이니 엔티티
   * @throws PtContractNotExistException 계약이 존재하지 않을 경우 예외 발생
   */
  private void validateContractExists(TrainerEntity trainer, TraineeEntity trainee) {
    if (!ptContractRepository.existsByTrainerIdAndTraineeId(trainer.getId(), trainee.getId())) {
      throw new PtContractNotExistException();
    }
  }

  /**
   * 트레이니 정보를 업데이트합니다.
   *
   * @param trainee 트레이니 엔티티
   * @param dto     트레이니 정보 수정 요청 DTO
   */
  private void updateTraineeInfo(TraineeEntity trainee, EditTraineeInfoRequestDto dto) {
    trainee.setBirthDate(dto.getBirthDate());
    trainee.setGender(dto.getGender());
    trainee.setHeight(dto.getHeight());
    trainee.setTargetType(dto.getTargetType());
    trainee.setTargetValue(dto.getTargetValue());
    trainee.setTargetReward(dto.getTargetReward());
  }

  /**
   * 남은 세션 수를 업데이트합니다.
   *
   * @param ptContract       PT 계약 엔티티
   * @param remainingSession 업데이트할 남은 세션 수
   */
  private void updateRemainingSession(PtContractEntity ptContract, int remainingSession) {
    int addition = remainingSession - ptContract.getRemainingSession();
    ptContract.addSession(addition);
  }
}