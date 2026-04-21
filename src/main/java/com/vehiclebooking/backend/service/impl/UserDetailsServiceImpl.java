
package com.vehiclebooking.backend.service.impl;

import com.vehiclebooking.backend.entity.User;
import com.vehiclebooking.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        System.out.println("User found: " + user.getEmail());
        System.out.println("Stored password hash: " + user.getPassword());
        System.out.println("User role: " + user.getRole());

        // if (!user.isActive()) {
        //     throw new DisabledException("Account is locked. Please contact Admin.");
        // }

        if (user.getRole() == null) {
            System.err.println("User " + email + " has no role assigned.");
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Collections.emptyList()
            );
        }

        var authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
        System.out.println("DEBUG: " + user.getFirstName() + "'s Authorities -> " + authorities);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
