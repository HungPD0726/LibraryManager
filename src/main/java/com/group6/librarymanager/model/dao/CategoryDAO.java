package com.group6.librarymanager.model.dao;

import com.group6.librarymanager.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryDAO extends JpaRepository<Category, Integer> {

    Optional<Category> findByCategoryName(String categoryName);
}
