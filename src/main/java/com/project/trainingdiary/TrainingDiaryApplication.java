package com.project.trainingdiary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TrainingDiaryApplication {

  public static void main(String[] args) {
    SpringApplication.run(TrainingDiaryApplication.class, args);
  }

}
