package com.project.trainingdiary.controller;

import com.project.trainingdiary.dto.request.CreatePtContractRequestDto;
import com.project.trainingdiary.dto.response.CommonResponse;
import com.project.trainingdiary.service.PtContractService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
