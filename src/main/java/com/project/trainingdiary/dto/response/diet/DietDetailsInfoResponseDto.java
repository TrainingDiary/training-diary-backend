package com.project.trainingdiary.dto.response.diet;

import com.project.trainingdiary.dto.response.comment.CommentDto;
import com.project.trainingdiary.entity.CommentEntity;
import com.project.trainingdiary.entity.DietEntity;
import com.project.trainingdiary.util.ConvertCloudFrontUrlUtil;
import java.time.LocalDate;
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
  private LocalDate createdDate;

  public static DietDetailsInfoResponseDto of(DietEntity diet, List<CommentEntity> comments) {

    List<CommentDto> commentDtoList = comments.stream()
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // 최신 댓글이 위로 오도록 정렬
        .map(CommentDto::fromEntity) // CommentEntity를 CommentDto로 변환
        .toList();

    return DietDetailsInfoResponseDto.builder()
        .id(diet.getId())
        .imageUrl(ConvertCloudFrontUrlUtil.convertToCloudFrontUrl(diet.getOriginalUrl()))
        .content(diet.getContent())
        .comments(commentDtoList)
        .createdDate(diet.getCreatedAt().toLocalDate())
        .build();
  }
}