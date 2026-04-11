package com.library.domain.repository;

import com.library.domain.model.Borrow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Integer> {

    @EntityGraph(attributePaths = {"student", "staff"})
    @Override
    java.util.Optional<Borrow> findById(Integer id);

    @EntityGraph(attributePaths = {"student", "staff"})
    @Override
    Page<Borrow> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"student", "staff"})
    Page<Borrow> findByStudentStudentId(Integer studentId, Pageable pageable);

    List<Borrow> findByStudentStudentIdOrderByBorrowIdDesc(Integer studentId);

    List<Borrow> findByStatus(String status);

    List<Borrow> findByStudentStudentIdAndStatusInOrderByBorrowIdDesc(Integer studentId, List<String> statuses);

    long countByStudentStudentIdAndStatusIn(Integer studentId, Collection<String> statuses);

    long countByStudentStudentIdAndStatus(Integer studentId, String status);

    @EntityGraph(attributePaths = {"student", "staff"})
    @Query("SELECT b FROM Borrow b WHERE b.status = :status")
    Page<Borrow> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.status = 'Pending'")
    long countPending();

    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.status = 'Borrowing'")
    long countBorrowing();

    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.status = 'Overdue'")
    long countOverdue();

    @Query("SELECT FUNCTION('MONTH', b.borrowDate) AS m, COUNT(b) FROM Borrow b " +
           "WHERE b.borrowDate >= :startDate GROUP BY FUNCTION('MONTH', b.borrowDate) " +
           "ORDER BY FUNCTION('MONTH', b.borrowDate)")
    List<Object[]> countByMonthSince(@Param("startDate") java.time.LocalDate startDate);

    @Query("SELECT b.status, COUNT(b) FROM Borrow b GROUP BY b.status")
    List<Object[]> countByStatusGroup();
}
