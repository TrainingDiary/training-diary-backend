package com.project.trainingdiary.model;

import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import com.project.trainingdiary.model.type.UserRoleType;
import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

  private final Long id;
  private final String email;
  private final String password;
  private final String name;
  private final UserRoleType role;
  private final boolean unreadNotification;
  private final Collection<? extends GrantedAuthority> authorities;
  private final boolean isTrainer;
  private final TrainerEntity trainer;
  private final TraineeEntity trainee;

  public static UserPrincipal create(TraineeEntity trainee) {
    return new UserPrincipal(
        trainee.getId(),
        trainee.getEmail(),
        trainee.getPassword(),
        trainee.getName(),
        UserRoleType.TRAINEE,
        trainee.isUnreadNotification(),
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINEE")),
        false,
        null,
        trainee
    );
  }

  public static UserPrincipal create(TrainerEntity trainer) {
    return new UserPrincipal(
        trainer.getId(),
        trainer.getEmail(),
        trainer.getPassword(),
        trainer.getName(),
        UserRoleType.TRAINER,
        trainer.isUnreadNotification(),
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER")),
        true,
        trainer,
        null
    );
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}