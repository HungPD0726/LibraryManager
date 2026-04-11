package com.library.domain.repository;

import com.library.domain.model.Category;
import com.library.feature.catalog.CategoryOptionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByCategoryName(String categoryName);

    @Query("""
            SELECT new com.library.feature.catalog.CategoryOptionView(c.categoryId, c.categoryName)
            FROM Category c
            ORDER BY c.categoryName, c.categoryId
            """)
    List<CategoryOptionView> findAllOptions();
}
