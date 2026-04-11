package com.library.domain.repository;

import com.library.domain.model.Role;
import com.library.feature.staff.RoleOptionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);

    @Query("""
            SELECT new com.library.feature.staff.RoleOptionView(r.roleId, r.roleName)
            FROM Role r
            ORDER BY r.roleName, r.roleId
            """)
    List<RoleOptionView> findAllOptions();
}
