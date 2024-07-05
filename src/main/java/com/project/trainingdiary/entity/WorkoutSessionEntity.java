package com.project.trainingdiary.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "workout_session")
public class WorkoutSessionEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private LocalDate sessionDate;

  private int sessionNumber;

  @Lob
  private String specialNote;

  @OneToMany(mappedBy = "workoutSession")
  private List<WorkoutEntity> workouts;

  @OneToMany(mappedBy = "workoutSession")
  private List<WorkoutMediaEntity> workoutMedia;

//  @ManyToOne
//  private PtContractEntity ptContract;

}

