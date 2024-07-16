package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.AddInBodyInfoRequestDto;
import com.project.trainingdiary.dto.request.EditTraineeInfoRequestDto;
import com.project.trainingdiary.dto.response.AddInBodyInfoResponseDto;
import com.project.trainingdiary.dto.response.EditTraineeInfoResponseDto;
import com.project.trainingdiary.dto.response.TraineeInfoResponseDto;
import com.project.trainingdiary.entity.InBodyRecordHistoryEntity;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.PtContractNotExistException;
import com.project.trainingdiary.exception.impl.TraineeNotExistException;
import com.project.trainingdiary.exception.impl.TrainerNotFoundException;
import com.project.trainingdiary.repository.InBodyRecordHistoryRepository;
import com.project.trainingdiary.repository.PtContractRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
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

  public TraineeInfoResponseDto getTraineeInfo(Long id) {
    TrainerEntity trainer = getAuthenticatedTrainer();
    TraineeEntity trainee = getTraineeById(id);

    checkContract(trainer, trainee);

    int totalSessions = ptContractRepository.findByTrainee(trainee).stream()
        .mapToInt(PtContractEntity::getTotalSession)
        .sum();
    int usedSessions = ptContractRepository.findByTrainee(trainee).stream()
        .mapToInt(PtContractEntity::getUsedSession)
        .sum();

    int remainingSessions = totalSessions - usedSessions;

    return TraineeInfoResponseDto.fromEntity(trainee, remainingSessions);
  }

  public AddInBodyInfoResponseDto addInBodyRecord(AddInBodyInfoRequestDto dto) {
    TrainerEntity trainer = getAuthenticatedTrainer();
    TraineeEntity trainee = getTraineeById(dto.getTraineeId());

    checkContract(trainer, trainee);

    InBodyRecordHistoryEntity inBodyRecord = AddInBodyInfoRequestDto.toEntity(dto, trainee);
    inBodyRecordHistoryRepository.save(inBodyRecord);
    return AddInBodyInfoResponseDto.fromEntity(inBodyRecord);
  }

  @Transactional
  public EditTraineeInfoResponseDto editTraineeInfo(EditTraineeInfoRequestDto dto) {
    TrainerEntity trainer = getAuthenticatedTrainer();
    TraineeEntity trainee = getTraineeById(dto.getTraineeId());

    checkContract(trainer, trainee);

    updateTraineeInfo(trainee, dto);

    return EditTraineeInfoResponseDto.fromEntity(trainee);
  }

  private TraineeEntity getTraineeById(Long id) {
    return traineeRepository.findById(id)
        .orElseThrow(TraineeNotExistException::new);
  }

  private TrainerEntity getAuthenticatedTrainer() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getName() == null) {
      throw new TrainerNotFoundException();
    }
    String email = authentication.getName();
    return trainerRepository.findByEmail(email)
        .orElseThrow(TrainerNotFoundException::new);
  }

  private void checkContract(TrainerEntity trainer, TraineeEntity trainee) {
    if (!ptContractRepository.existsByTrainerIdAndTraineeId(trainer.getId(), trainee.getId())) {
      throw new PtContractNotExistException();
    }
  }

  private void updateTraineeInfo(TraineeEntity trainee, EditTraineeInfoRequestDto dto) {
    trainee.setBirthDate(dto.getBirthDate());
    trainee.setGender(dto.getGender());
    trainee.setHeight(dto.getHeight());
    trainee.setTargetType(dto.getTargetType());
    trainee.setTargetValue(dto.getTargetValue());
    trainee.setTargetReward(dto.getTargetReward());
  }
}