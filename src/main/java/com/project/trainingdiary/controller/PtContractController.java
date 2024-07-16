package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.AddPtContractSessionRequestDto;
import com.project.trainingdiary.dto.request.CreatePtContractRequestDto;
import com.project.trainingdiary.dto.request.TerminatePtContractRequestDto;
import com.project.trainingdiary.dto.response.PtContractResponseDto;
import com.project.trainingdiary.model.PtContractSort;
import com.project.trainingdiary.service.PtContractService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/pt-contracts")
public class PtContractController {

  private final PtContractService ptContractService;

  @PostMapping
  public ResponseEntity<Void> createPtContract(
      @RequestBody @Valid CreatePtContractRequestDto dto
  ) {
    ptContractService.createPtContract(dto);
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<Page<PtContractResponseDto>> getPtContractList(
      Pageable pageable,
      @RequestParam PtContractSort sortBy
  ) {
    Page<PtContractResponseDto> ptContracts = ptContractService.getPtContractList(pageable, sortBy);
    return ResponseEntity.ok(ptContracts);
  }

  @GetMapping("/{id}")
  public ResponseEntity<PtContractResponseDto> getPtContract(
      @PathVariable long id
  ) {
    PtContractResponseDto ptContract = ptContractService.getPtContract(id);
    return ResponseEntity.ok(ptContract);
  }

  @PostMapping("/add-session")
  public ResponseEntity<Void> addPtContractSession(
      @RequestBody @Valid AddPtContractSessionRequestDto dto
  ) {
    ptContractService.addPtContractSession(dto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/terminate")
  public ResponseEntity<Void> terminatePtContract(
      @RequestBody @Valid TerminatePtContractRequestDto dto
  ) {
    ptContractService.terminatePtContract(dto);
    return ResponseEntity.ok().build();
  }
}
