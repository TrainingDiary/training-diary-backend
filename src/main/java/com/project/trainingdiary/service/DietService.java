package com.project.trainingdiary.service;

import com.project.trainingdiary.dto.request.CreateDietRequestDto;
import com.project.trainingdiary.dto.response.DietImageResponseDto;
import com.project.trainingdiary.entity.DietEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.exception.impl.InvalidFileTypeException;
import com.project.trainingdiary.exception.impl.TraineeNotExistException;
import com.project.trainingdiary.repository.DietRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.util.ImageUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DietService {

  private final DietRepository dietRepository;
  private final TraineeRepository traineeRepository;
  private final ImageUtil imageUtil;

  @Transactional
  public DietImageResponseDto createDiet(CreateDietRequestDto dto) throws IOException {
    TraineeEntity trainee = getTrainee();
    List<String> originalUrls = new ArrayList<>();
    List<String> thumbnailUrls = new ArrayList<>();

    for (MultipartFile imageFile : dto.getImages()) {
      if (!imageUtil.isValidImageType(imageFile)) {
        throw new InvalidFileTypeException();
      }

      String originalUrl = imageUtil.uploadImageToS3(imageFile);
      String extension = imageUtil.getExtension(imageFile.getOriginalFilename());
      String thumbnailUrl = imageUtil.createAndUploadThumbnail(imageFile, originalUrl, extension);
      originalUrls.add(originalUrl);
      thumbnailUrls.add(thumbnailUrl);
    }

    DietEntity diet = new DietEntity();
    diet.setTrainee(trainee);
    diet.setContent(dto.getContent());
    diet.setOriginalUrl(String.join(",", originalUrls)); // 여러 이미지 URL을 콤마로 구분
    diet.setThumbnailUrl(String.join(",", thumbnailUrls)); // 여러 썸네일 URL을 콤마로 구분

    dietRepository.save(diet);

    return DietImageResponseDto.builder()
        .originalUrl(originalUrls)
        .thumbnailUrl(thumbnailUrls)
        .build();
  }

  private TraineeEntity getTrainee() {
    return traineeRepository
        .findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
        .orElseThrow(TraineeNotExistException::new);
  }
}