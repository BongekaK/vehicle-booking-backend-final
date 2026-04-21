package com.vehiclebooking.backend.repository;

import com.vehiclebooking.backend.entity.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRepository extends JpaRepository<AuditEntry, Long> {
}
