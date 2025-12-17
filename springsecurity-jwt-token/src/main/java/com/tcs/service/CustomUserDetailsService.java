package com.tcs.service;


import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        if ("user1".equals(username)) {
            return new User(
                    "user1",
                    "{noop}user123",
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }

        if ("admin".equals(username)) {
            return new User(
                    "admin",
                    "{noop}admin123",
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        throw new UsernameNotFoundException("User not found");
    }
}