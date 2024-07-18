package com.project.trainingdiary.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "diet")
@Getter
@Setter
public class DietEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  private String content;

  @Column(length = 2000)
  private String originalUrl;

  @Column(length = 2000)
  private String thumbnailUrl;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "trainee_id")
  private TraineeEntity trainee;
}
