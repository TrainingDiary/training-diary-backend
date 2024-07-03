package com.project.trainingdiary.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessMessage {

  NO_DUPLICATE_EMAIL(HttpStatus.OK, "No duplicate email."),
  SENT_VERIFICATION_SUCCESS(HttpStatus.OK, "Sent verification email."),
  VERIFICATION_SUCCESS(HttpStatus.OK, "Verification success.");

  private final HttpStatus status;
  private final String message;
}
