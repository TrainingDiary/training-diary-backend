package com.project.trainingdiary.dto.response.comment;

import com.project.trainingdiary.entity.CommentEntity;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentDto {

  private Long id;
  private String comment;
  private LocalDate createdDate;

  public static CommentDto fromEntity(CommentEntity comment) {
    return CommentDto.builder()
        .id(comment.getId())
        .comment(comment.getComment())
        .createdDate(comment.getCreatedAt().toLocalDate())
        .build();
  }
}