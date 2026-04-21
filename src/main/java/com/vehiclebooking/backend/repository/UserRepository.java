package com.vehiclebooking.backend.repository;

import com.vehiclebooking.backend.entity.User;
import com.vehiclebooking.backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findByRole(UserRole role);
    List<User> findByLineManagerId(UUID lineManagerId);
    Optional<User> findByEmail(String email);
}
