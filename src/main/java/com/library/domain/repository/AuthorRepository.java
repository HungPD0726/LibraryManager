package com.library.domain.repository;

import com.library.domain.model.Author;
import com.library.feature.catalog.AuthorOptionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
    Optional<Author> findByAuthorName(String authorName);

    @Query("""
            SELECT new com.library.feature.catalog.AuthorOptionView(a.authorId, a.authorName)
            FROM Author a
            ORDER BY a.authorName, a.authorId
            """)
    List<AuthorOptionView> findAllOptions();
}
