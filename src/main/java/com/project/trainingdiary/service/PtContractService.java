package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.AddPtContractSessionRequestDto;
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
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  /**
   * PT 계약 생성
   */
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

  /**
   * PT 계약을 목록으로 조회
   */
  public Page<PtContractResponseDto> getPtContractList(Pageable pageable) {
    //TODO: 연관된 트레이너, 트레이니 이름 추가. 이름순 정렬
    if (getMyRole().equals(UserRoleType.TRAINEE)) {
      return ptContractRepository.findByTraineeEmail(getEmail(), pageable)
          .map(PtContractEntity::toResponseDto);
    } else {
      return ptContractRepository.findByTrainerEmail(getEmail(), pageable)
          .map(PtContractEntity::toResponseDto);
    }
  }

  /**
   * PT 계약을 id로 조회
   */
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

  /**
   * PT 계약 횟수를 업데이트 함
   */
  @Transactional
  public void addPtContractSession(AddPtContractSessionRequestDto dto) {
    TrainerEntity trainer = getTrainer();

    // 1개의 계약만 있다는 것을 가정함
    PtContractEntity ptContract = ptContractRepository
        .findByTrainerIdAndTraineeId(trainer.getId(), dto.getTraineeId())
        .orElseThrow(PtContractNotExistException::new);

    ptContract.addSession(dto.getAddition());
    ptContractRepository.save(ptContract);
  }

  //TODO: usedSession 업데이트를 해야함 - 예약 시간이 지나는 순간에 하면 좋을 듯

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
