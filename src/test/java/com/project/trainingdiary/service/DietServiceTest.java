package com.project.trainingdiary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.trainingdiary.dto.request.CreateDietRequestDto;
import com.project.trainingdiary.dto.response.DietImageResponseDto;
import com.project.trainingdiary.entity.DietEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.exception.impl.InvalidFileTypeException;
import com.project.trainingdiary.exception.impl.TraineeNotExistException;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.UserRoleType;
import com.project.trainingdiary.repository.DietRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.util.ImageUtil;
import io.awspring.cloud.s3.S3Operations;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DietServiceTest {

  @Mock
  private DietRepository dietRepository;

  @Mock
  private TraineeRepository traineeRepository;

  @Mock
  private ImageUtil imageUtil;

  @Mock
  private S3Operations s3Operations;

  @InjectMocks
  private DietService dietService;

  private TraineeEntity trainee;

  @BeforeEach
  public void setUp() {
    setupTrainee();
  }

  private void setupTrainee() {
    trainee = TraineeEntity.builder()
        .id(10L)
        .email("trainee@example.com")
        .name("김트레이니")
        .role(UserRoleType.TRAINEE)
        .build();
  }

  private void setupTraineeAuth() {
    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_TRAINEE");
    Collection authorities = Collections.singleton(authority);

    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getAuthorities()).thenReturn(authorities);

    UserDetails userDetails = UserPrincipal.create(trainee);
    lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
    lenient().when(authentication.getName()).thenReturn(trainee.getEmail());

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    lenient().when(traineeRepository.findByEmail(trainee.getEmail()))
        .thenReturn(Optional.of(trainee));
  }


  @AfterEach
  void cleanup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("이미지 업로드 실패 - 파일 타입이 맞지 않으면 예외 발생")
  void testCreateDietFailInvalidFileType() throws IOException {
    setupTraineeAuth();

    when(traineeRepository.findByEmail("trainee@example.com")).thenReturn(Optional.of(trainee));

    MockMultipartFile mockFile = new MockMultipartFile(
        "file", "test.txt", "text/plain", "test text content".getBytes());

    CreateDietRequestDto dto = CreateDietRequestDto.builder()
        .content("Test content")
        .images(List.of(mockFile))
        .build();

    assertThrows(InvalidFileTypeException.class,
        () -> dietService.createDiet(dto));

    ArgumentCaptor<DietEntity> dietCaptor = ArgumentCaptor.forClass(DietEntity.class);
    ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);

    verify(dietRepository, never()).save(dietCaptor.capture());
    verify(imageUtil, never()).uploadImageToS3(any());
    verify(imageUtil, never()).createAndUploadThumbnail(any(), any(), any());

    assertTrue(dietCaptor.getAllValues().isEmpty());
    assertTrue(bucketCaptor.getAllValues().isEmpty());
    assertTrue(keyCaptor.getAllValues().isEmpty());
    assertTrue(inputStreamCaptor.getAllValues().isEmpty());
  }

  @Test
  @DisplayName("이미지 업로드 성공")
  void testCreateDietSuccess() throws IOException {
    setupTraineeAuth();

    when(traineeRepository.findByEmail("trainee@example.com")).thenReturn(Optional.of(trainee));

    BufferedImage img = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setColor(Color.RED);
    g2d.fillRect(0, 0, 500, 500);
    g2d.dispose();

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(img, "jpg", byteArrayOutputStream);
    byte[] imageBytes = byteArrayOutputStream.toByteArray();
    MockMultipartFile mockFile = new MockMultipartFile(
        "file", "test.jpg", "image/jpeg", imageBytes);

    CreateDietRequestDto dto = CreateDietRequestDto.builder()
        .content("Test content")
        .images(List.of(mockFile))
        .build();

    when(imageUtil.isValidImageType(mockFile)).thenReturn(true);

    when(imageUtil.uploadImageToS3(mockFile)).thenReturn(
        "https://test-bucket.s3.amazonaws.com/original.jpg");

    when(imageUtil.getExtension("test.jpg")).thenReturn("jpg");
    when(imageUtil.createAndUploadThumbnail(mockFile,
        "https://test-bucket.s3.amazonaws.com/original.jpg", "jpg"))
        .thenReturn("https://test-bucket.s3.amazonaws.com/thumb_original.jpg");

    DietImageResponseDto response = dietService.createDiet(dto);

    ArgumentCaptor<DietEntity> dietCaptor = ArgumentCaptor.forClass(DietEntity.class);
    verify(dietRepository, times(1)).save(dietCaptor.capture());
    DietEntity savedDiet = dietCaptor.getValue();

    assertEquals("Test content", savedDiet.getContent());
    assertEquals(trainee, savedDiet.getTrainee());
    assertEquals("https://test-bucket.s3.amazonaws.com/original.jpg", savedDiet.getOriginalUrl());
    assertEquals("https://test-bucket.s3.amazonaws.com/thumb_original.jpg",
        savedDiet.getThumbnailUrl());

    assertNotNull(response);
    assertEquals(1, response.getOriginalUrl().size());
    assertEquals(1, response.getThumbnailUrl().size());
    assertTrue(
        response.getOriginalUrl().contains("https://test-bucket.s3.amazonaws.com/original.jpg"));
    assertTrue(response.getThumbnailUrl()
        .contains("https://test-bucket.s3.amazonaws.com/thumb_original.jpg"));
  }

  @Test
  @DisplayName("트레이니를 찾을 수 없음 - 예외 발생")
  void testCreateDietTraineeNotFound() {
    setupTraineeAuth();

    when(traineeRepository.findByEmail("trainee@example.com")).thenReturn(Optional.empty());

    MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg",
        "image content".getBytes());

    CreateDietRequestDto dto = CreateDietRequestDto.builder()
        .content("Test content")
        .images(List.of(image))
        .build();

    assertThrows(TraineeNotExistException.class, () -> dietService.createDiet(dto));
  }
}