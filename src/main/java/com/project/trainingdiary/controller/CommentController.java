package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.comment.AddCommentRequestDto;
import com.project.trainingdiary.dto.request.comment.UpdateCommentRequestDto;
import com.project.trainingdiary.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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

  @Operation(
      summary = "식단 댓글 추가",
      description = "식단 댓글를 추가합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
  })
  @PostMapping
  @PreAuthorize("hasRole('TRAINER')")
  public ResponseEntity<Void> addTrainerComment(
      @RequestBody @Valid AddCommentRequestDto dto
  ) {
    commentService.addTrainerComment(dto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "식단 댓글 변경",
      description = "식단 댓글를 변경합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
  })
  @PutMapping
  @PreAuthorize("hasRole('TRAINER')")
  public ResponseEntity<Void> updateTrainerComment(
      @RequestBody @Valid UpdateCommentRequestDto dto
  ) {
    commentService.updateTrainerComment(dto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "식단 댓글 삭제",
      description = "식단 댓글를 삭제합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
  })
  @PreAuthorize("hasRole('TRAINER')")
  @DeleteMapping("{id}")
  public ResponseEntity<Void> deleteTrainerComment(
      @PathVariable Long id
  ) {
    commentService.deleteTrainerComment(id);
    return ResponseEntity.ok().build();
  }
}
