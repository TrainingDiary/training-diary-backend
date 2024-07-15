package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.AddPtContractSessionRequestDto;
import com.project.trainingdiary.dto.request.CreatePtContractRequestDto;
import com.project.trainingdiary.dto.request.TerminatePtContractRequestDto;
import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.dto.response.PtContractResponseDto;
import com.project.trainingdiary.model.PtContractSort;
import com.project.trainingdiary.service.PtContractService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  public CommonResponse<?> createPtContract(
      @RequestBody @Valid CreatePtContractRequestDto dto
  ) {
    ptContractService.createPtContract(dto);
    return CommonResponse.created();
  }

  @GetMapping
  public CommonResponse<?> getPtContractList(
      Pageable pageable,
      @RequestParam PtContractSort sortBy
  ) {
    Page<PtContractResponseDto> ptContracts = ptContractService.getPtContractList(pageable, sortBy);
    return CommonResponse.success(ptContracts);
  }

  @GetMapping("/{id}")
  public CommonResponse<?> getPtContract(
      @PathVariable long id
  ) {
    PtContractResponseDto ptContract = ptContractService.getPtContract(id);
    return CommonResponse.success(ptContract);
  }

  @PostMapping("/add-session")
  public CommonResponse<?> addPtContractSession(
      @RequestBody @Valid AddPtContractSessionRequestDto dto
  ) {
    ptContractService.addPtContractSession(dto);
    return CommonResponse.success();
  }

  @PostMapping("/terminate")
  public CommonResponse<?> terminatePtContract(
      @RequestBody @Valid TerminatePtContractRequestDto dto
  ) {
    ptContractService.terminatePtContract(dto);
    return CommonResponse.success();
  }
}
