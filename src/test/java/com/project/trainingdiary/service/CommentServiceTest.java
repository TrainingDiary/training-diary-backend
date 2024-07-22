package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.comment.AddCommentRequestDto;
import com.project.trainingdiary.entity.CommentEntity;
import com.project.trainingdiary.entity.DietEntity;
import com.project.trainingdiary.entity.TrainerEntity;
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

  @BeforeEach
  public void setup() {
    setupTrainer();
    setupDiet();
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
    dto.setDietId(diet.getId());
    dto.setComment("Great job!");

    // when
    when(dietRepository.findById(dto.getDietId())).thenReturn(Optional.of(diet));
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
    dto.setDietId(99L);  // Non-existent diet ID
    dto.setComment("Great job!");

    // when
    when(dietRepository.findById(dto.getDietId())).thenReturn(Optional.empty());

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
    dto.setDietId(diet.getId());
    dto.setComment("Great job!");

    // when
    when(trainerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    // then
    assertThrows(TrainerNotFoundException.class, () -> commentService.addTrainerComment(dto));
  }
}