package com.library.domain.repository;

import com.library.domain.model.BookFile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookFileRepository extends JpaRepository<BookFile, Integer> {

    @EntityGraph(attributePaths = {"book", "staff"})
    List<BookFile> findAllByOrderByBookFileIdDesc();

    @EntityGraph(attributePaths = {"book", "staff"})
    List<BookFile> findByBookBookIdAndIsActiveTrueOrderByUploadAtDescBookFileIdDesc(Integer bookId);

    @EntityGraph(attributePaths = {"book", "staff"})
    Optional<BookFile> findByBookFileIdAndIsActiveTrue(Integer bookFileId);
}
