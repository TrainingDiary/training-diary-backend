package com.project.trainingdiary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class NotificationMessage {

  private String title;
  private String body;
}
