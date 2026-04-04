package com.library.domain.repository;

import com.library.domain.model.BorrowItem;
import com.library.domain.model.BorrowItemId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BorrowItemRepository extends JpaRepository<BorrowItem, BorrowItemId> {

    @EntityGraph(attributePaths = {"book"})
    List<BorrowItem> findByBorrowId(Integer borrowId);

    @Query(value = "SELECT TOP(:limit) bi.BookID, b.BookName, SUM(bi.Quantity) AS total " +
                   "FROM BorrowItem bi JOIN Book b ON bi.BookID = b.BookID " +
                   "GROUP BY bi.BookID, b.BookName ORDER BY total DESC",
           nativeQuery = true)
    List<Object[]> findTopBorrowedBooks(@Param("limit") int limit);

    @Query(value = "SELECT TOP(:limit) br.StudentID, s.StudentName, COUNT(br.BorrowID) AS total " +
                   "FROM Borrow br JOIN Student s ON br.StudentID = s.StudentID " +
                   "GROUP BY br.StudentID, s.StudentName ORDER BY total DESC",
           nativeQuery = true)
    List<Object[]> findTopBorrowers(@Param("limit") int limit);
}
