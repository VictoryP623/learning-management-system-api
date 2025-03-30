package com.example.learning_management_system_api.config;

import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.service.IUserService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {
  @Autowired private IUserService userService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<User> user = userService.getUserByEmail(username);
    if (user.isPresent()) {
      return new CustomUserDetails(user.get());
    }
    throw new UsernameNotFoundException(username);
  }
}