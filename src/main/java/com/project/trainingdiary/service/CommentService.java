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

  /**
   * 트레이너가 새로운 댓글을 추가합니다.
   *
   * @param dto 댓글 추가 요청 DTO
   * @throws DietNotExistException    주어진 식단 ID에 해당하는 식단이 없을 경우 예외 발생
   * @throws TrainerNotFoundException 인증된 트레이너가 존재하지 않을 경우 예외 발생
   */
  public void addTrainerComment(AddCommentRequestDto dto) {
    TrainerEntity trainer = getAuthenticatedTrainer();

    // 주어진 식단 ID에 해당하는 식단 조회 및 예외 처리
    DietEntity diet = dietRepository.findById(dto.getId())
        .orElseThrow(DietNotExistException::new);

    // 새로운 댓글 엔티티 생성 및 저장
    CommentEntity comment = CommentEntity.builder()
        .comment(dto.getComment())
        .trainer(trainer)
        .diet(diet)
        .build();

    commentRepository.save(comment);
  }

  /**
   * 트레이너가 기존 댓글을 수정합니다.
   *
   * @param dto 댓글 수정 요청 DTO
   * @throws CommentNotExistException     주어진 댓글 ID에 해당하는 댓글이 없을 경우 예외 발생
   * @throws UnauthorizedCommentException 댓글 작성자가 현재 인증된 트레이너가 아닐 경우 예외 발생
   * @throws TrainerNotFoundException     인증된 트레이너가 존재하지 않을 경우 예외 발생
   */
  public void updateTrainerComment(UpdateCommentRequestDto dto) {
    TrainerEntity trainer = getAuthenticatedTrainer();

    // 주어진 댓글 ID에 해당하는 댓글 조회 및 예외 처리
    CommentEntity comment = commentRepository.findById(dto.getId())
        .orElseThrow(CommentNotExistException::new);

    // 댓글 작성자가 현재 인증된 트레이너가 아닐 경우 예외 발생
    if (!comment.getTrainer().equals(trainer)) {
      throw new UnauthorizedCommentException();
    }

    comment.setComment(dto.getComment());
    commentRepository.save(comment);
  }

  /**
   * 트레이너가 댓글을 삭제합니다.
   *
   * @param id 삭제할 댓글 ID
   * @throws CommentNotExistException     주어진 댓글 ID에 해당하는 댓글이 없을 경우 예외 발생
   * @throws UnauthorizedCommentException 댓글 작성자가 현재 인증된 트레이너가 아닐 경우 예외 발생
   * @throws TrainerNotFoundException     인증된 트레이너가 존재하지 않을 경우 예외 발생
   */
  public void deleteTrainerComment(Long id) {
    TrainerEntity trainer = getAuthenticatedTrainer();

    // 주어진 댓글 ID에 해당하는 댓글 조회 및 예외 처리
    CommentEntity comment = commentRepository.findById(id)
        .orElseThrow(CommentNotExistException::new);

    // 댓글 작성자가 현재 인증된 트레이너가 아닐 경우 예외 발생
    if (!comment.getTrainer().equals(trainer)) {
      throw new UnauthorizedCommentException();
    }

    commentRepository.delete(comment);
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