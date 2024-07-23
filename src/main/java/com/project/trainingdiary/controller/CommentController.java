package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.comment.AddCommentRequestDto;
import com.project.trainingdiary.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "8 - Comment API", description = "트레이너의 식단 댓글 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/comments/")
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  @PreAuthorize("hasRole('TRAINER')")
  public ResponseEntity<Void> addTrainerComment(
      @RequestBody @Valid AddCommentRequestDto dto
  ) {
    commentService.addTrainerComment(dto);
    return ResponseEntity.ok().build();
  }

  @PutMapping
  public ResponseEntity<Void> updateTrainerComment() {
    return ResponseEntity.ok().build();
  }

  @PostMapping("{id}")
  public ResponseEntity<Void> deleteTrainerComment(
      @PathVariable Long id
  ) {
    return ResponseEntity.ok().build();
  }
}
