package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    @EntityGraph(attributePaths = {"category", "publisher", "authors"})
    @Override
    Page<Book> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "publisher", "authors"})
    @Query("SELECT b FROM Book b WHERE LOWER(b.bookName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByName(@Param("keyword") String keyword, Pageable pageable);

    Page<Book> findByCategoryCategoryId(Integer categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "publisher", "authors"})
    @Query("""
        SELECT DISTINCT b
        FROM Book b
        LEFT JOIN b.authors a
        WHERE (:search IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:letter IS NULL OR UPPER(SUBSTRING(b.bookName, 1, 1)) = UPPER(:letter))
          AND (:categoryId IS NULL OR b.category.categoryId = :categoryId)
          AND (:publisherId IS NULL OR b.publisher.publisherId = :publisherId)
          AND (:authorKeyword IS NULL OR LOWER(a.authorName) LIKE LOWER(CONCAT('%', :authorKeyword, '%')))
        """)
    Page<Book> searchCatalog(@Param("search") String search,
                             @Param("letter") String letter,
                             @Param("categoryId") Integer categoryId,
                             @Param("publisherId") Integer publisherId,
                             @Param("authorKeyword") String authorKeyword,
                             Pageable pageable);

    @EntityGraph(attributePaths = {"category", "publisher", "authors"})
    @Query("SELECT b FROM Book b WHERE b.bookId = :bookId")
    java.util.Optional<Book> findDetailedByBookId(@Param("bookId") Integer bookId);

    @Query("SELECT COUNT(b) FROM Book b")
    long countAllBooks();

    @Query("SELECT COALESCE(SUM(b.available), 0) FROM Book b")
    long countTotalAvailable();
}
