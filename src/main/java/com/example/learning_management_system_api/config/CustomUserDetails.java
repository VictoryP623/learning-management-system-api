package com.example.learning_management_system_api.config;

import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.utils.enums.UserStatus;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails, Principal {

  private User user;
  
  public CustomUserDetails(User user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
  }

  public Long getUserId() {
    return user.getId();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isEnabled() {
    return !user.getStatus().equals(UserStatus.DEACTIVE);
  }

  @Override
  public String getName() {
    return user.getFullname();
  }
}
