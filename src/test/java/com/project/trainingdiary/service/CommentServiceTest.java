package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.comment.AddCommentRequestDto;
import com.project.trainingdiary.dto.request.comment.UpdateCommentRequestDto;
import com.project.trainingdiary.entity.CommentEntity;
import com.project.trainingdiary.entity.DietEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.comment.CommentNotExistException;
import com.project.trainingdiary.exception.comment.UnauthorizedCommentException;
import com.project.trainingdiary.exception.diet.DietNotExistException;
import com.project.trainingdiary.exception.user.TrainerNotFoundException;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.repository.CommentRepository;
import com.project.trainingdiary.repository.DietRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private DietRepository dietRepository;

  @Mock
  private TrainerRepository trainerRepository;

  @InjectMocks
  private CommentService commentService;

  private TrainerEntity trainer;
  private DietEntity diet;
  private CommentEntity comment;

  @BeforeEach
  public void setup() {
    setupTrainer();
    setupDiet();
    setupComment();
  }

  private void setupTrainer() {
    trainer = TrainerEntity.builder()
        .id(1L)
        .email("trainer@example.com")
        .name("Trainer")
        .build();
  }

  private void setupDiet() {
    diet = DietEntity.builder()
        .id(1L)
        .content("Sample Diet")
        .build();
  }

  private void setupComment() {
    DietEntity diet = DietEntity.builder()
        .id(1L)
        .content("Sample Diet")
        .build();

    comment = CommentEntity.builder()
        .id(1L)
        .comment("Initial Comment")
        .trainer(trainer)
        .diet(diet)
        .build();
  }

  private void setupTrainerAuth() {
    // 트레이너의 인증정보가 들어있는 상태
    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_TRAINER");
    Collection authorities = Collections.singleton(authority);

    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getAuthorities()).thenReturn(authorities);

    UserDetails userDetails = UserPrincipal.create(trainer);
    lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
    lenient().when(authentication.getName()).thenReturn(trainer.getEmail());
    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    lenient().when(trainerRepository.findByEmail(trainer.getEmail()))
        .thenReturn(Optional.of(trainer));
  }

  @Test
  @DisplayName("트레이너 댓글 추가 - 성공")
  void addTrainerComment_Success() {
    // given
    setupTrainerAuth();
    AddCommentRequestDto dto = new AddCommentRequestDto();
    dto.setId(diet.getId());
    dto.setComment("Great job!");

    // when
    when(dietRepository.findById(dto.getId())).thenReturn(Optional.of(diet));
    ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);

    // execute
    commentService.addTrainerComment(dto);

    // then
    verify(commentRepository).save(captor.capture());
    CommentEntity savedComment = captor.getValue();
    assertEquals(dto.getComment(), savedComment.getComment());
    assertEquals(trainer, savedComment.getTrainer());
    assertEquals(diet, savedComment.getDiet());
  }

  @Test
  @DisplayName("트레이너 댓글 추가 - 실패(존재하지 않는 식단)")
  void addTrainerComment_Fail_DietNotExist() {
    // given
    setupTrainerAuth();
    AddCommentRequestDto dto = new AddCommentRequestDto();
    dto.setId(99L);  // Non-existent diet ID
    dto.setComment("Great job!");

    // when
    when(dietRepository.findById(dto.getId())).thenReturn(Optional.empty());

    // then
    assertThrows(DietNotExistException.class, () -> commentService.addTrainerComment(dto));
  }

  @Test
  @DisplayName("트레이너 댓글 추가 - 실패(존재하지 않는 트레이너)")
  void addTrainerComment_Fail_TrainerNotFound() {
    // given
    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getName()).thenReturn("unknown@example.com");
    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    AddCommentRequestDto dto = new AddCommentRequestDto();
    dto.setId(diet.getId());
    dto.setComment("Great job!");

    // when
    when(trainerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    // then
    assertThrows(TrainerNotFoundException.class, () -> commentService.addTrainerComment(dto));
  }

  @Test
  @DisplayName("트레이너 댓글 업데이트 - 성공")
  void updateTrainerComment_Success() {
    // given
    setupTrainerAuth();
    UpdateCommentRequestDto dto = new UpdateCommentRequestDto();
    dto.setId(comment.getId());
    dto.setComment("Updated Comment");

    // when
    when(commentRepository.findById(dto.getId())).thenReturn(Optional.of(comment));
    ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);

    // execute
    commentService.updateTrainerComment(dto);

    // then
    verify(commentRepository).save(captor.capture());
    CommentEntity updatedComment = captor.getValue();
    assertEquals(dto.getComment(), updatedComment.getComment());
  }

  @Test
  @DisplayName("트레이너 댓글 업데이트 - 실패(존재하지 않는 댓글)")
  void updateTrainerComment_Fail_CommentNotExist() {
    // given
    setupTrainerAuth();
    UpdateCommentRequestDto dto = new UpdateCommentRequestDto();
    dto.setId(99L);  // Non-existent comment ID
    dto.setComment("Updated Comment");

    // when
    when(commentRepository.findById(dto.getId())).thenReturn(Optional.empty());

    // then
    assertThrows(CommentNotExistException.class, () -> commentService.updateTrainerComment(dto));
  }

  @Test
  @DisplayName("트레이너 댓글 업데이트 - 실패(권한 없음)")
  void updateTrainerComment_Fail_Unauthorized() {
    // given
    setupTrainerAuth();
    TrainerEntity anotherTrainer = TrainerEntity.builder()
        .id(2L)
        .email("another_trainer@example.com")
        .name("Another Trainer")
        .build();

    CommentEntity anotherComment = CommentEntity.builder()
        .id(comment.getId())
        .comment("Initial Comment")
        .trainer(anotherTrainer)
        .diet(comment.getDiet())
        .build();

    UpdateCommentRequestDto dto = new UpdateCommentRequestDto();
    dto.setId(anotherComment.getId());
    dto.setComment("Updated Comment");

    // when
    when(commentRepository.findById(dto.getId())).thenReturn(Optional.of(anotherComment));

    // then
    assertThrows(UnauthorizedCommentException.class, () -> commentService.updateTrainerComment(dto));
  }
}