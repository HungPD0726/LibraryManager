package com.library.repository;

import com.library.entity.BookPrice;
import com.library.entity.BookPriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BookPriceRepository extends JpaRepository<BookPrice, BookPriceId> {

    @Query("SELECT bp FROM BookPrice bp WHERE bp.bookId = :bookId AND bp.endDate IS NULL")
    Optional<BookPrice> findCurrentByBookId(@Param("bookId") Integer bookId);
}
