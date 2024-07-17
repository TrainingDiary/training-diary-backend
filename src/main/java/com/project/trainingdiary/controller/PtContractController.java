package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.CreatePtContractRequestDto;
import com.project.trainingdiary.dto.request.TerminatePtContractRequestDto;
import com.project.trainingdiary.dto.response.CreatePtContractResponseDto;
import com.project.trainingdiary.dto.response.PtContractResponseDto;
import com.project.trainingdiary.model.PtContractSort;
import com.project.trainingdiary.service.PtContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "6 - PT 계약")
@RestController
@AllArgsConstructor
@RequestMapping("api/pt-contracts")
public class PtContractController {

  private final PtContractService ptContractService;

  @Operation(
      summary = "PT 계약을 생성",
      description = "트레이너가 트레이니와 PT 계약을 생성함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "409", description = "트레이너와 트레이니가 이미 계약이 있습니다.", content = @Content)
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping
  public ResponseEntity<CreatePtContractResponseDto> createPtContract(
      @RequestBody @Valid CreatePtContractRequestDto dto
  ) {
    return ResponseEntity.ok(ptContractService.createPtContract(dto));
  }

  @Operation(
      summary = "PT 계약 목록을 조회",
      description = "자신의 PT 계약 목록을 조회함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공")
  })
  @GetMapping
  public ResponseEntity<Page<PtContractResponseDto>> getPtContractList(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam PtContractSort sortBy
  ) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(ptContractService.getPtContractList(pageable, sortBy));
  }

  @Operation(
      summary = "PT 계약 종료",
      description = "트레이너가 PT 계약을 종료함"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "404", description = "계약이 없습니다.")
  })
  @PreAuthorize("hasRole('TRAINER')")
  @PostMapping("/terminate")
  public ResponseEntity<Void> terminatePtContract(
      @RequestBody @Valid TerminatePtContractRequestDto dto
  ) {
    ptContractService.terminatePtContract(dto);
    return ResponseEntity.ok().build();
  }
}
