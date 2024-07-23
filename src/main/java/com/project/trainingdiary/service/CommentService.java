package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.comment.AddCommentRequestDto;
import com.project.trainingdiary.dto.request.comment.UpdateCommentRequestDto;
import com.project.trainingdiary.entity.CommentEntity;
import com.project.trainingdiary.entity.DietEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.comment.CommentNotExistException;
import com.project.trainingdiary.exception.comment.UnauthorizedCommentException;
import com.project.trainingdiary.exception.diet.DietNotExistException;
import com.project.trainingdiary.exception.user.TrainerNotFoundException;
import com.project.trainingdiary.repository.CommentRepository;
import com.project.trainingdiary.repository.DietRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final DietRepository dietRepository;
  private final TrainerRepository trainerRepository;

  public void addTrainerComment(AddCommentRequestDto dto) {

    TrainerEntity trainer = getAuthenticatedTrainer();

    DietEntity diet = dietRepository.findById(dto.getId())
        .orElseThrow(DietNotExistException::new);

    CommentEntity comment = CommentEntity.builder()
        .comment(dto.getComment())
        .trainer(trainer)
        .diet(diet)
        .build();

    commentRepository.save(comment);
  }


  public void updateTrainerComment(UpdateCommentRequestDto dto) {
    TrainerEntity trainer = getAuthenticatedTrainer();

    CommentEntity comment = commentRepository.findById(dto.getId())
        .orElseThrow(CommentNotExistException::new);

    if (!comment.getTrainer().equals(trainer)) {
      throw new UnauthorizedCommentException();
    }

    comment.setComment(dto.getComment());
    commentRepository.save(comment);
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
}
