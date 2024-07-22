package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.ptcontract.AddPtContractSessionRequestDto;
import com.project.trainingdiary.dto.request.ptcontract.CreatePtContractRequestDto;
import com.project.trainingdiary.dto.request.ptcontract.TerminatePtContractRequestDto;
import com.project.trainingdiary.dto.response.ptcontract.CreatePtContractResponseDto;
import com.project.trainingdiary.dto.response.ptcontract.PtContractResponseDto;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.ptcontract.PtContractAlreadyExistException;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.model.PtContractSort;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PtContractService {

  private final PtContractRepository ptContractRepository;
  private final TrainerRepository trainerRepository;
  private final TraineeRepository traineeRepository;

  /**
   * PT 계약 생성
   */
  public CreatePtContractResponseDto createPtContract(CreatePtContractRequestDto dto) {
    TrainerEntity trainer = getTrainer();
    TraineeEntity trainee = getTrainee(dto.getTraineeEmail());

    if (ptContractRepository.existsByTrainerIdAndTraineeId(trainer.getId(), trainee.getId())) {
      throw new PtContractAlreadyExistException();
    }

    PtContractEntity ptContract = PtContractEntity.of(trainer, trainee, 0);
    ptContractRepository.save(ptContract);

    return new CreatePtContractResponseDto(ptContract.getId());
  }

  /**
   * PT 계약을 목록으로 조회
   */
  public Page<PtContractResponseDto> getPtContractList(Pageable pageable, PtContractSort sortBy) {
    if (getMyRole().equals(UserRoleType.TRAINEE)) {
      return ptContractRepository.findByTraineeEmail(getEmail(), pageable, sortBy)
          .map(PtContractEntity::toResponseDto);
    } else {
      return ptContractRepository.findByTrainerEmail(getEmail(), pageable, sortBy)
          .map(PtContractEntity::toResponseDto);
    }
  }

  /**
   * PT 계약 횟수를 업데이트 함
   */
  public void addPtContractSession(AddPtContractSessionRequestDto dto) {
    TrainerEntity trainer = getTrainer();

    // 1개의 계약만 있다는 것을 가정함
    PtContractEntity ptContract = ptContractRepository
        .findByTrainerIdAndTraineeId(trainer.getId(), dto.getTraineeId())
        .orElseThrow(PtContractNotExistException::new);

    ptContract.addSession(dto.getAddition());
    ptContractRepository.save(ptContract);
  }

  /**
   * PT 계약을 종료함
   */
  public void terminatePtContract(TerminatePtContractRequestDto dto) {
    PtContractEntity ptContract = ptContractRepository.findByIdAndIsTerminatedFalse(
            dto.getPtContractId())
        .orElseThrow(PtContractNotExistException::new);

    ptContract.terminate();
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
