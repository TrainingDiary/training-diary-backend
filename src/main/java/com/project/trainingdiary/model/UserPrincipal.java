package com.project.trainingdiary.model;

import com.project.trainingdiary.entity.TraineeEntity;
import com.project.trainingdiary.entity.TrainerEntity;
import java.util.Collection;
import java.util.Collections;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@AllArgsConstructor
public class UserPrincipal implements UserDetails {

  private final String email;
  private final String password;
  private final Collection<? extends GrantedAuthority> authorities;

  public static UserPrincipal create(TraineeEntity trainee) {
    return new UserPrincipal(
        trainee.getEmail(),
        trainee.getPassword(),
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINEE"))
    );
  }

  public static UserPrincipal create(TrainerEntity trainer) {
    return new UserPrincipal(
        trainer.getEmail(),
        trainer.getPassword(),
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TRAINER"))
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
