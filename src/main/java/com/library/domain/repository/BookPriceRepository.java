package com.library.domain.repository;

import com.library.domain.model.BookPrice;
import com.library.domain.model.BookPriceId;
import com.library.feature.catalog.PriceDisplayView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookPriceRepository extends JpaRepository<BookPrice, BookPriceId> {

    @Query("""
        SELECT bp
        FROM BookPrice bp
        JOIN FETCH bp.price
        WHERE bp.bookId = :bookId
          AND bp.endDate IS NULL
        """)
    Optional<BookPrice> findCurrentByBookId(@Param("bookId") Integer bookId);

    @Query("""
        SELECT bp
        FROM BookPrice bp
        JOIN FETCH bp.price
        WHERE bp.bookId IN :bookIds
          AND bp.endDate IS NULL
        """)
    List<BookPrice> findCurrentByBookIds(@Param("bookIds") Collection<Integer> bookIds);

    @Query("""
            SELECT new com.library.feature.catalog.PriceDisplayView(
                b.bookId,
                b.bookName,
                b.available,
                p.amount,
                p.currency,
                p.note
            )
            FROM BookPrice bp
            JOIN bp.book b
            JOIN bp.price p
            WHERE bp.endDate IS NULL
            ORDER BY b.bookName, b.bookId
            """)
    List<PriceDisplayView> findCurrentCatalogPriceViews();
}
