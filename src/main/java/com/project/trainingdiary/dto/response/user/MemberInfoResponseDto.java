package com.project.trainingdiary.dto.response.user;

import com.project.trainingdiary.model.type.UserRoleType;
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
