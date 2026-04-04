package com.library.domain.repository;

import com.library.domain.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Optional<Staff> findByUsername(String username);
    Optional<Staff> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
