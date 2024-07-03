package com.project.trainingdiary.entity;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.model.UserRoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trainee")
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

  @Column(nullable = false)
  @Temporal(TemporalType.DATE)
  private Date birthDate;

  @Column(nullable = false)
  private String gender;

  private int totalSessions;

  private double currentWeight;

  private double currentSkeletalMuscleMass;

  private double currentBodyFatPercentage;

  private double targetWeight;

  private double targetSkeletalMuscleMass;

  private double targetBodyFatPercentage;

  private String targetReward;
}
