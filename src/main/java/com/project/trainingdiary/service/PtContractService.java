package com.project.trainingdiary.service;

import com.project.trainingdiary.component.FcmPushNotification;
import com.project.trainingdiary.dto.request.ptcontract.AddPtContractSessionRequestDto;
import com.project.trainingdiary.dto.request.ptcontract.CreatePtContractRequestDto;
import com.project.trainingdiary.dto.request.ptcontract.TerminatePtContractRequestDto;
import com.project.trainingdiary.dto.response.ptcontract.CreatePtContractResponseDto;
import com.project.trainingdiary.dto.response.ptcontract.PtContractResponseDto;
import com.project.trainingdiary.entity.NotificationEntity;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.notification.UnsupportedNotificationTypeException;
import com.project.trainingdiary.exception.ptcontract.PtContractAlreadyExistException;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.ptcontract.PtContractTrainerEmailNotExistException;
import com.project.trainingdiary.exception.user.UserNotFoundException;
import com.project.trainingdiary.model.NotificationMessage;
import com.project.trainingdiary.model.PtContractSort;
import com.project.trainingdiary.model.type.NotificationType;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.NotificationRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.util.NotificationMessageGeneratorUtil;
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
  private final NotificationRepository notificationRepository;
  private final FcmPushNotification fcmPushNotification;

  /**
   * PT 계약 생성
   */
  public CreatePtContractResponseDto createPtContract(CreatePtContractRequestDto dto) {
    TrainerEntity trainer = getTrainer();
    TraineeEntity trainee = traineeRepository.findByEmail(dto.getTraineeEmail())
        .orElseThrow(PtContractTrainerEmailNotExistException::new);

    if (ptContractRepository.existsByTrainerIdAndTraineeId(trainer.getId(), trainee.getId())) {
      throw new PtContractAlreadyExistException();
    }

    PtContractEntity ptContract = PtContractEntity.of(trainer, trainee, 0);
    ptContractRepository.save(ptContract);

    NotificationEntity notification = saveNotification(
        NotificationType.PT_CONTRACT_CREATED,
        trainer,
        trainee
    );
    sendNotification(notification);

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

  /**
   * 알림 엔티티를 만들어서 저장함
   */
  private NotificationEntity saveNotification(
      NotificationType notificationType,
      TrainerEntity trainer,
      TraineeEntity trainee
  ) {
    NotificationMessage message;
    if (notificationType == NotificationType.PT_CONTRACT_CREATED) {
      message = NotificationMessageGeneratorUtil.createPtContract(
          trainer.getName(), trainee.getName()
      );
    } else {
      throw new UnsupportedNotificationTypeException();
    }
    NotificationEntity notification = NotificationEntity.of(
        notificationType, false, true,
        trainer, trainee, message.getBody(), message.getTitle(),
        null
    );
    notificationRepository.save(notification);
    return notification;
  }

  /**
   * 알림을 전송하고, 전송한 사용자에게 미확인 알림 표시를 함
   */
  private void sendNotification(NotificationEntity notification) {
    fcmPushNotification.sendPushNotification(notification);
    if (notification.isToTrainee()) {
      notification.getTrainee().setUnreadNotification(true);
    }
    if (notification.isToTrainer()) {
      notification.getTrainer().setUnreadNotification(true);
    }
  }
}
