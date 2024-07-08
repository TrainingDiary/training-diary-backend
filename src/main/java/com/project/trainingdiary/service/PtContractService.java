package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.CreatePtContractRequestDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.PtContractAlreadyExistException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.repository.PtContractRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PtContractService {

  private final PtContractRepository ptContractRepository;
  private final TrainerRepository trainerRepository;
  private final TraineeRepository traineeRepository;

  @Transactional
  public void createPtContract(CreatePtContractRequestDto dto) {
    TrainerEntity trainer = getTrainer(); // 이 메서드를 호출한 사람은 트레이너임
    TraineeEntity trainee = getTrainee(dto.getTraineeEmail());

    if (ptContractRepository.existsByTrainerIdAndTraineeId(trainer.getId(), trainee.getId())) {
      throw new PtContractAlreadyExistException();
    }

    PtContractEntity ptContract = PtContractEntity.of(trainer, trainee, dto.getSessionCount());
    ptContractRepository.save(ptContract);
  }

  private TrainerEntity getTrainer() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_TRAINER"))) {
      throw new UserNotFoundException();
    }
    return trainerRepository.findByEmail(auth.getName())
        .orElseThrow(UserNotFoundException::new);
  }

  private TraineeEntity getTrainee(String email) {
    return traineeRepository.findByEmail(email)
        .orElseThrow(UserNotFoundException::new);
  }
}
