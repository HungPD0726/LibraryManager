package com.library.repository;

import com.library.entity.BookFile;
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
