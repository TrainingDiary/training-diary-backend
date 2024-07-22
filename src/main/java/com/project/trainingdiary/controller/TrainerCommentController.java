package com.project.trainingdiary.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "8 - Trainer Comment API", description = "트레이너의 식단 댓글 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/trainer-comments/")
public class TrainerCommentController {

  @PostMapping
  public ResponseEntity<Void> createTrainerComment() {
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
