package com.sysluna.api.infrastructure.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.sysluna.api.domain.exception.UnauthorizedException;
import com.sysluna.api.domain.model.Role;
import com.sysluna.api.domain.model.User;
import com.sysluna.api.infrastructure.repository.UserRepository;

@Component
public class CurrentUserProvider {

  private final UserRepository userRepository;

  public CurrentUserProvider(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      throw new UnauthorizedException("Authenticated user not found.");
    }

    User user = userRepository.findByEmail(authentication.getName());
    if (user == null) {
      throw new UnauthorizedException("Authenticated user not found.");
    }
    return user;
  }

  public boolean isAdmin() {
    return getCurrentUser().getRole() == Role.ADMIN;
  }

  public boolean isPlatformAdmin() {
    return getCurrentUser().isPlatformAdmin();
  }

  /** Same as isPlatformAdmin(), but false (not an exception) when nobody is logged in. */
  public boolean isAuthenticatedPlatformAdmin() {
    try {
      return isPlatformAdmin();
    } catch (UnauthorizedException e) {
      return false;
    }
  }

  public void requireSelfOrAdmin(String userId) {
    User current = getCurrentUser();
    if (current.getRole() != Role.ADMIN && !current.getId().equals(userId)) {
      throw new UnauthorizedException("You are not allowed to access another user's data.");
    }
  }
}
