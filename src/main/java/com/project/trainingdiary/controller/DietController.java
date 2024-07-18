package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.CreateDietRequestDto;
import com.project.trainingdiary.dto.response.DietImageResponseDto;
import com.project.trainingdiary.service.DietService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
  public ResponseEntity<DietImageResponseDto> createDiet(
      @RequestPart("images") List<MultipartFile> images,
      @RequestPart("content") String content
  ) throws IOException {
    CreateDietRequestDto dto = CreateDietRequestDto.builder()
        .images(images)
        .content(content)
        .build();

    DietImageResponseDto response = dietService.createDiet(dto);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('TRAINEE')")
  @GetMapping
  public ResponseEntity<List<DietImageResponseDto>> getDiets() {
    List<DietImageResponseDto> diets = dietService.getDiets();
    return ResponseEntity.ok(diets);
  }

}