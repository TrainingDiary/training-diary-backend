package com.project.trainingdiary.dto.response;

import com.project.trainingdiary.model.UserRoleType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberInfoResponseDto {

  private long id;
  private String email;
  private String name;
  private UserRoleType role;
  private boolean unreadNotification;
}
