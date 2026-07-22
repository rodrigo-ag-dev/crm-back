package com.sysluna.api.infrastructure.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sysluna.api.domain.model.Tenant;
import com.sysluna.api.infrastructure.repository.TenantRepository;
import com.sysluna.api.infrastructure.repository.UserRepository;
import com.sysluna.api.infrastructure.tenant.TenantSchemaNames;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final TenantRepository tenantRepository;

  public CustomUserDetailsService(UserRepository userRepository, TenantRepository tenantRepository) {
    this.userRepository = userRepository;
    this.tenantRepository = tenantRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    com.sysluna.api.domain.model.User user = userRepository.findByEmail(email);

    if (user == null) {
      throw new UsernameNotFoundException("User not found with email: " + email);
    }

    Tenant tenant = tenantRepository.findById(user.getTenantId())
        .orElseThrow(() -> new UsernameNotFoundException("Tenant not found for user: " + email));

    if (!tenant.isActive()) {
      throw new UsernameNotFoundException("Tenant is inactive for user: " + email);
    }

    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

    return new AppUserPrincipal(
        user.getEmail(),
        user.getPasswordHash(),
        user.isActive(),
        user.isMustChangePassword(),
        tenant.getId(),
        TenantSchemaNames.forSlug(tenant.getSlug()),
        authorities);
  }
}
