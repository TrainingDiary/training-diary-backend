package com.project.trainingdiary.entity;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.model.type.GenderType;
import com.project.trainingdiary.model.type.TargetType;
import com.project.trainingdiary.model.type.UserRoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "trainee")
public class TraineeEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  @Column(name = "trainee_id")
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String password;

  @Enumerated(STRING)
  @Column(nullable = false)
  private UserRoleType role;

  @Temporal(TemporalType.DATE)
  private LocalDate birthDate;

  @Enumerated(STRING)
  private GenderType gender;

  private double height;

  @OneToMany(mappedBy = "trainee", cascade = ALL, orphanRemoval = true)
  private List<InBodyRecordHistoryEntity> inBodyRecords;

  @Enumerated(STRING)
  private TargetType targetType;

  private double targetValue;

  private String targetReward;

  @OneToOne(fetch = LAZY)
  private FcmTokenEntity fcmToken;

  private boolean unreadNotification;
}
