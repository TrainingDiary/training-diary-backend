package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.CreatePtContractRequestDto;
import com.project.trainingdiary.dto.response.PtContractResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.impl.PtContractAlreadyExistException;
import com.project.trainingdiary.exception.impl.PtContractNotExistException;
import com.project.trainingdiary.exception.impl.UserNotFoundException;
import com.project.trainingdiary.model.UserRoleType;
import com.project.trainingdiary.repository.PtContractRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import java.util.List;
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

  public List<PtContractResponseDto> getPtContractList() {
    if (getMyRole().equals(UserRoleType.TRAINEE)) {
      return ptContractRepository.findByTrainee_Email(getEmail()).stream()
          .map(PtContractEntity::toResponseDto)
          .toList();
    } else {
      return ptContractRepository.findByTrainer_Email(getEmail()).stream()
          .map(PtContractEntity::toResponseDto)
          .toList();
    }
  }

  public PtContractResponseDto getPtContract(long id) {
    PtContractEntity ptContract = ptContractRepository.findById(id)
        .orElseThrow(PtContractNotExistException::new);

    if (getMyRole().equals(UserRoleType.TRAINEE)) {
      if (!ptContract.getTrainee().getEmail().equals(getEmail())) {
        throw new PtContractNotExistException();
      }
    } else {
      if (!ptContract.getTrainer().getEmail().equals(getEmail())) {
        throw new PtContractNotExistException();
      }
    }

    return ptContract.toResponseDto();
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

  private UserRoleType getMyRole() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRAINER"))) {
      return UserRoleType.TRAINER;
    } else {
      return UserRoleType.TRAINEE;
    }
  }

  private String getEmail() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }
}
