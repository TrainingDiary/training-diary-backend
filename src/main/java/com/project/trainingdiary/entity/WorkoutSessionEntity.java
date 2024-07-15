package com.project.trainingdiary.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import com.project.trainingdiary.dto.request.WorkoutSessionCreateRequestDto;
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
import lombok.ToString;

@Builder(toBuilder = true)
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

}


