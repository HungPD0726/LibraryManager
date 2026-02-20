package com.group6.librarymanager.model.dao;

import com.group6.librarymanager.model.entity.BorrowItem;
import com.group6.librarymanager.model.entity.BorrowItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowItemDAO extends JpaRepository<BorrowItem, BorrowItemId> {
}
