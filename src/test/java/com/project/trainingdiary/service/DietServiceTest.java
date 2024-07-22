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

import com.project.trainingdiary.dto.request.diet.CreateDietRequestDto;
import com.project.trainingdiary.dto.response.diet.DietDetailsInfoResponseDto;
import com.project.trainingdiary.dto.response.diet.DietImageResponseDto;
import com.project.trainingdiary.entity.DietEntity;
import com.project.trainingdiary.entity.PtContractEntity;
import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.exception.diet.DietNotExistException;
import com.project.trainingdiary.exception.ptcontract.PtContractNotExistException;
import com.project.trainingdiary.exception.user.TraineeNotExistException;
import com.project.trainingdiary.exception.workout.InvalidFileTypeException;
import com.project.trainingdiary.model.UserPrincipal;
import com.project.trainingdiary.model.type.UserRoleType;
import com.project.trainingdiary.repository.DietRepository;
import com.project.trainingdiary.repository.TraineeRepository;
import com.project.trainingdiary.repository.TrainerRepository;
import com.project.trainingdiary.repository.ptContract.PtContractRepository;
import com.project.trainingdiary.util.ImageUtil;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
  private TrainerRepository trainerRepository;

  @Mock
  private PtContractRepository ptContractRepository;

  @Mock
  private ImageUtil imageUtil;


  @InjectMocks
  private DietService dietService;

  private TraineeEntity trainee;
  private TrainerEntity trainer;

  @BeforeEach
  public void setUp() {
    setupTrainee();
    setupTrainer();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setupTrainee() {
    trainee = TraineeEntity.builder()
        .id(10L)
        .email("trainee@example.com")
        .name("김트레이니")
        .role(UserRoleType.TRAINEE)
        .build();
  }

  private void setupTrainer() {
    trainer = TrainerEntity.builder()
        .id(20L)
        .email("trainer@example.com")
        .name("김트레이너")
        .role(UserRoleType.TRAINER)
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

  private void setupTrainerAuth() {
    GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_TRAINER");
    Collection authorities = Collections.singleton(authority);

    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getAuthorities()).thenReturn(authorities);

    UserDetails userDetails = UserPrincipal.create(trainer);
    lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
    lenient().when(authentication.getName()).thenReturn(trainer.getEmail());

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    lenient().when(trainerRepository.findByEmail(trainer.getEmail()))
        .thenReturn(Optional.of(trainer));
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
        .image(mockFile)
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
        .image(mockFile)
        .build();

    when(imageUtil.isValidImageType(mockFile)).thenReturn(true);

    when(imageUtil.uploadImageToS3(mockFile)).thenReturn(
        "https://test-bucket.s3.amazonaws.com/original.jpg");

    when(imageUtil.getExtension("test.jpg")).thenReturn("jpg");
    when(imageUtil.createAndUploadThumbnail(mockFile,
        "https://test-bucket.s3.amazonaws.com/original.jpg", "jpg"))
        .thenReturn("https://test-bucket.s3.amazonaws.com/thumb_original.jpg");

    dietService.createDiet(dto);

    ArgumentCaptor<DietEntity> dietCaptor = ArgumentCaptor.forClass(DietEntity.class);
    verify(dietRepository, times(1)).save(dietCaptor.capture());
    DietEntity savedDiet = dietCaptor.getValue();

    assertEquals("Test content", savedDiet.getContent());
    assertEquals(trainee, savedDiet.getTrainee());
    assertEquals("https://test-bucket.s3.amazonaws.com/original.jpg", savedDiet.getOriginalUrl());
    assertEquals("https://test-bucket.s3.amazonaws.com/thumb_original.jpg",
        savedDiet.getThumbnailUrl());

    verify(imageUtil, times(1)).uploadImageToS3(mockFile);
    verify(imageUtil, times(1)).createAndUploadThumbnail(mockFile,
        "https://test-bucket.s3.amazonaws.com/original.jpg", "jpg");
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
        .image(image)
        .build();

    assertThrows(TraineeNotExistException.class, () -> dietService.createDiet(dto));
  }

  @Test
  @DisplayName("트레이니가 자신의 식단을 조회할 수 있음")
  void testGetDietsForTrainee() {
    setupTraineeAuth();
    Pageable pageable = PageRequest.of(0, 10);
    DietEntity diet = new DietEntity();
    diet.setId(1L);
    diet.setTrainee(trainee);
    diet.setContent("Test content");
    diet.setThumbnailUrl("https://test-bucket.s3.amazonaws.com/thumb_original.jpg");
    Page<DietEntity> dietPage = new PageImpl<>(Collections.singletonList(diet), pageable, 1);
    when(dietRepository.findByTraineeId(trainee.getId(), pageable)).thenReturn(dietPage);

    Page<DietImageResponseDto> response = dietService.getDiets(trainee.getId(), pageable);

    assertNotNull(response);
    assertEquals(1, response.getTotalElements());
    DietImageResponseDto responseDto = response.getContent().get(0);
    assertEquals(diet.getId(), responseDto.getDietId());
    assertTrue(responseDto.getThumbnailUrl()
        .contains("https://test-bucket.s3.amazonaws.com/thumb_original.jpg"));
  }

  @Test
  @DisplayName("트레이너가 트레이니의 식단을 조회할 수 없음 - 계약이 없는 경우")
  void testGetDietsForTrainerWithoutContract() {
    setupTrainerAuth();

    TraineeEntity traineeToView = new TraineeEntity();
    traineeToView.setId(1L);
    when(traineeRepository.findById(1L)).thenReturn(Optional.of(traineeToView));

    Pageable pageable = PageRequest.of(0, 10);
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), traineeToView.getId()))
        .thenReturn(Optional.empty());

    assertThrows(PtContractNotExistException.class,
        () -> dietService.getDiets(traineeToView.getId(), pageable));
  }

  @Test
  @DisplayName("사용자를 찾을 수 없음 - 예외 발생")
  void testGetDietsUserNotFound() {
    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getName()).thenReturn("unknown @example.com");

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    assertThrows(TraineeNotExistException.class,
        () -> dietService.getDiets(999L, PageRequest.of(0, 10)));
  }

  @Test
  @DisplayName("트레이니가 자신의 식단 상세 정보를 조회할 수 있음")
  void testGetDietDetailsForTraineeSuccess() {
    setupTraineeAuth();

    DietEntity diet = new DietEntity();
    diet.setId(1L);
    diet.setTrainee(trainee);
    diet.setContent("Test content");
    diet.setOriginalUrl("https://test-bucket.s3.amazonaws.com/original.jpg");

    when(dietRepository.findByTraineeIdAndId(trainee.getId(), diet.getId())).thenReturn(
        Optional.of(diet));

    DietDetailsInfoResponseDto response = dietService.getDietDetails(diet.getId());

    assertNotNull(response);
    assertEquals(diet.getId(), response.getId());
    assertEquals(diet.getContent(), response.getContent());
    assertEquals(diet.getOriginalUrl(), response.getImageUrl());
  }

  @Test
  @DisplayName("트레이니가 자신의 식단 상세 정보를 조회할 수 없음 - 식단 존재하지 않음")
  void testGetDietDetailsForTraineeFailDietNotExist() {
    setupTraineeAuth();

    when(dietRepository.findByTraineeIdAndId(trainee.getId(), 1L)).thenReturn(Optional.empty());

    assertThrows(DietNotExistException.class, () -> dietService.getDietDetails(1L));
  }

  @Test
  @DisplayName("트레이니가 다른 트레이니의 식단 상세 정보를 조회할 수 없음")
  void testGetDietDetailsForTraineeFailOtherTrainee() {
    setupTraineeAuth();

    TraineeEntity otherTrainee = TraineeEntity.builder()
        .id(20L)
        .email("othertrainee@example.com")
        .name("다른 트레이니")
        .role(UserRoleType.TRAINEE)
        .build();

    DietEntity diet = new DietEntity();
    diet.setId(1L);
    diet.setTrainee(otherTrainee);
    diet.setContent("Other trainee's content");
    diet.setOriginalUrl("https://test-bucket.s3.amazonaws.com/other.jpg");

    when(dietRepository.findByTraineeIdAndId(otherTrainee.getId(), diet.getId())).thenReturn(
        Optional.of(diet));

    assertThrows(DietNotExistException.class, () -> dietService.getDietDetails(diet.getId()));
  }

  @Test
  @DisplayName("트레이너가 트레이니의 식단 상세 정보를 조회할 수 있음")
  void testGetDietDetailsForTrainerSuccess() {
    setupTrainerAuth();

    TraineeEntity traineeToView = new TraineeEntity();
    traineeToView.setId(1L);

    DietEntity diet = new DietEntity();
    diet.setId(1L);
    diet.setTrainee(traineeToView);
    diet.setContent("Test content");
    diet.setOriginalUrl("https://test-bucket.s3.amazonaws.com/original.jpg");

    when(dietRepository.findById(diet.getId())).thenReturn(Optional.of(diet));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), traineeToView.getId()))
        .thenReturn(Optional.of(new PtContractEntity()));

    DietDetailsInfoResponseDto response = dietService.getDietDetails(diet.getId());

    assertNotNull(response);
    assertEquals(diet.getId(), response.getId());
    assertEquals(diet.getContent(), response.getContent());
    assertEquals(diet.getOriginalUrl(), response.getImageUrl());
  }

  @Test
  @DisplayName("트레이너가 트레이니의 식단 상세 정보를 조회할 수 없음 - 계약이 없는 경우")
  void testGetDietDetailsForTrainerFailNoContract() {
    setupTrainerAuth();

    TraineeEntity traineeToView = new TraineeEntity();
    traineeToView.setId(1L);

    DietEntity diet = new DietEntity();
    diet.setId(1L);
    diet.setTrainee(traineeToView);

    when(dietRepository.findById(diet.getId())).thenReturn(Optional.of(diet));
    when(ptContractRepository.findByTrainerIdAndTraineeId(trainer.getId(), traineeToView.getId()))
        .thenReturn(Optional.empty());

    assertThrows(PtContractNotExistException.class, () -> dietService.getDietDetails(diet.getId()));
  }

  @Test
  @DisplayName("식단 상세 정보 조회 실패 - 사용자가 트레이니 또는 트레이너가 아닌 경우")
  void testGetDietDetailsFailUserNotFound() {
    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getName()).thenReturn("unknown@example.com");

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    assertThrows(TraineeNotExistException.class, () -> dietService.getDietDetails(1L));
  }

  @Test
  @DisplayName("트레이니가 자신의 식단을 성공적으로 삭제")
  void testDeleteDietSuccess() {
    setupTraineeAuth();

    DietEntity diet = new DietEntity();
    diet.setId(1L);
    diet.setTrainee(trainee);

    when(dietRepository.findByTraineeIdAndId(trainee.getId(), diet.getId())).thenReturn(
        Optional.of(diet));

    dietService.deleteDiet(diet.getId());

    verify(dietRepository, times(1)).delete(diet);
  }

  @Test
  @DisplayName("트레이니가 자신의 식단을 삭제할 수 없음 - 식단 존재하지 않음")
  void testDeleteDietFailDietNotExist() {
    setupTraineeAuth();

    when(dietRepository.findByTraineeIdAndId(trainee.getId(), 1L)).thenReturn(Optional.empty());

    assertThrows(DietNotExistException.class, () -> dietService.deleteDiet(1L));

    verify(dietRepository, never()).delete(any());
  }

  @Test
  @DisplayName("트레이니가 다른 트레이니의 식단을 삭제할 수 없음")
  void testDeleteDietFailOtherTrainee() {
    setupTraineeAuth();

    TraineeEntity otherTrainee = TraineeEntity.builder()
        .id(20L)
        .email("othertrainee@example.com")
        .name("다른 트레이니")
        .role(UserRoleType.TRAINEE)
        .build();

    DietEntity diet = new DietEntity();
    diet.setId(1L);
    diet.setTrainee(otherTrainee);

    when(dietRepository.findByTraineeIdAndId(otherTrainee.getId(), diet.getId())).thenReturn(
        Optional.of(diet));

    assertThrows(DietNotExistException.class, () -> dietService.deleteDiet(diet.getId()));

    verify(dietRepository, never()).delete(any());
  }

  @Test
  @DisplayName("식단 삭제 실패 - 인증된 사용자가 트레이니가 아닌 경우")
  void testDeleteDietFailUserNotFound() {
    Authentication authentication = mock(Authentication.class);
    lenient().when(authentication.getName()).thenReturn("unknown@example.com");

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    assertThrows(TraineeNotExistException.class, () -> dietService.deleteDiet(1L));

    verify(dietRepository, never()).delete(any());
  }
}