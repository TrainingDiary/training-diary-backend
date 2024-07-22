package com.project.trainingdiary.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.dto.request.workout.session.WorkoutSessionCreateRequestDto;
import com.project.trainingdiary.dto.request.workout.session.WorkoutSessionUpdateRequestDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity(name = "workout_session")
public class WorkoutSessionEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private LocalDate sessionDate;

  private int sessionNumber;

  @Lob
  private String specialNote;

  @OneToMany
  @JoinColumn(name = "workout_session_id")
  private List<WorkoutEntity> workouts;

  @OneToMany
  @JoinColumn(name = "workout_session_id")
  private List<WorkoutMediaEntity> workoutMedia;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "pt_contract_id")
  private PtContractEntity ptContract;

  public static WorkoutSessionEntity toEntity(
      WorkoutSessionCreateRequestDto dto,
      List<WorkoutEntity> workouts,
      PtContractEntity ptContract
  ) {

    return WorkoutSessionEntity.builder()
        .sessionDate(dto.getSessionDate())
        .sessionNumber(dto.getSessionNumber())
        .specialNote(dto.getSpecialNote())
        .workouts(workouts)
        .ptContract(ptContract)
        .build();

  }

  public static WorkoutSessionEntity updateEntity(
      WorkoutSessionUpdateRequestDto dto,
      List<WorkoutEntity> workouts,
      WorkoutSessionEntity workoutSession
  ) {
    return workoutSession.toBuilder()
        .sessionDate(dto.getSessionDate())
        .specialNote(dto.getSpecialNote())
        .workouts(workouts)
        .build();
  }

}


