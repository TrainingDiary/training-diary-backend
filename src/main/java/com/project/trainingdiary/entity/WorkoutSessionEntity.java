package com.project.trainingdiary.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Entity(name = "WorkoutSession")
public class WorkoutSessionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDate sessionDate;

  private int sessionNumber;

  @Lob
  private String specialNote;

  @OneToMany(mappedBy = "workoutSession")
  private List<WorkoutEntity> workouts;

  @OneToMany(mappedBy = "workoutSession")
  private List<WorkoutMediaEntity> workoutMedia;

  @OneToOne
  private ScheduleEntity schedule;

  @ManyToOne
  private PtContractEntity ptContract;

}
