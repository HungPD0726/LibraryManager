package com.group6.librarymanager.model.dao;

import com.group6.librarymanager.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleDAO extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(String roleName);
}
