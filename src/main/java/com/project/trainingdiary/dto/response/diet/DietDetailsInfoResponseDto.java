package com.project.trainingdiary.dto.response.diet;

import com.project.trainingdiary.dto.response.comment.CommentDto;
import com.project.trainingdiary.entity.CommentEntity;
import com.project.trainingdiary.entity.DietEntity;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DietDetailsInfoResponseDto {

  private Long id;
  private String imageUrl;
  private String content;
  private List<CommentDto> comments;
  private LocalDateTime createdDate;

  public static DietDetailsInfoResponseDto of(DietEntity diet,
      List<CommentEntity> comments) {

    List<CommentDto> commentDtos = comments.stream()
        .map(CommentDto::fromEntity)
        .toList();

    return DietDetailsInfoResponseDto.builder()
        .id(diet.getId())
        .imageUrl(diet.getOriginalUrl())
        .content(diet.getContent())
        .comments(commentDtos)
        .createdDate(diet.getCreatedAt())
        .build();
  }
}
