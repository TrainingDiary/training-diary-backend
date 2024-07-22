package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.CreateDietRequestDto;
import com.project.trainingdiary.dto.response.DietDetailsInfoResponseDto;
import com.project.trainingdiary.dto.response.DietImageResponseDto;
import com.project.trainingdiary.service.DietService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "7 - Diet API", description = "트레이니의 식단 관련 API")
@RestController
@RequestMapping("api/diets/")
@RequiredArgsConstructor
public class DietController {

  private final DietService dietService;

  @Operation(
      summary = "식단 생성", description = "트레이니가 이미지를 업로드하고 식단 내용을 추가하여 식단을 생성합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
  })
  @PreAuthorize("hasRole('TRAINEE')")
  @PostMapping
  public ResponseEntity<Void> createDiet(
      @RequestPart("image") MultipartFile image,
      @RequestPart("content") String content
  ) throws IOException {
    CreateDietRequestDto dto = CreateDietRequestDto.builder()
        .image(image)
        .content(content)
        .build();

    dietService.createDiet(dto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "식단 목록 조회",
      description = "트레이니의 식단 목록을 페이징하여 조회합니다."
  )
  @GetMapping("{id}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
  })
  public ResponseEntity<Page<DietImageResponseDto>> getTraineeDiets(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "9") int size
  ) {
    Sort.Direction direction = Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));
    Page<DietImageResponseDto> diets = dietService.getDiets(id, pageable);

    return ResponseEntity.ok(diets);
  }

  @Operation(
      summary = "식단 상세",
      description = "식단의 상세을 표시합니다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
  })
  @GetMapping("{id}/details")
  public ResponseEntity<DietDetailsInfoResponseDto> getDietDetails(
      @PathVariable Long id
  ) {
    DietDetailsInfoResponseDto diet = dietService.getDietDetails(id);
    return ResponseEntity.ok(diet);
  }
}