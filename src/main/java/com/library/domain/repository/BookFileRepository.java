package com.library.domain.repository;

import com.library.domain.model.BookFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookFileRepository extends JpaRepository<BookFile, Integer> {

    List<BookFile> findByBookBookIdOrderByUploadAtDescBookFileIdDesc(Integer bookId);

    List<BookFile> findByBookBookIdAndIsActiveTrueOrderByUploadAtDescBookFileIdDesc(Integer bookId);

    Optional<BookFile> findByBookFileIdAndIsActiveTrue(Integer bookFileId);
}
