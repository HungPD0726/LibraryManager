package com.group6.librarymanager.model.dao;

import com.group6.librarymanager.model.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowDAO extends JpaRepository<Borrow, Integer> {
    List<Borrow> findByStatus(String status);
}
