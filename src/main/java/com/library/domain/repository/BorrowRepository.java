package com.library.domain.repository;

import com.library.domain.model.Borrow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @EntityGraph(attributePaths = {"student"})
    @Query("""
            SELECT b
            FROM Borrow b
            WHERE b.status = :status
              AND b.dueDate = :dueDate
              AND b.dueReminderSentDate IS NULL
            ORDER BY b.borrowId ASC
            """)
    List<Borrow> findDueSoonReminderCandidates(@Param("status") String status,
                                               @Param("dueDate") LocalDate dueDate);

    @EntityGraph(attributePaths = {"student"})
    @Query("""
            SELECT b
            FROM Borrow b
            WHERE b.status IN :statuses
              AND b.dueDate < :today
              AND b.overdueReminderSentDate IS NULL
            ORDER BY b.borrowId ASC
            """)
    List<Borrow> findOverdueReminderCandidates(@Param("statuses") Collection<String> statuses,
                                               @Param("today") LocalDate today);

    @Modifying
    @Query("""
            UPDATE Borrow b
            SET b.dueReminderSentDate = :processedOn
            WHERE b.borrowId = :borrowId
              AND b.status = :status
              AND b.dueDate = :dueDate
              AND b.dueReminderSentDate IS NULL
            """)
    int markDueReminderSent(@Param("borrowId") Integer borrowId,
                            @Param("status") String status,
                            @Param("dueDate") LocalDate dueDate,
                            @Param("processedOn") LocalDate processedOn);

    @Modifying
    @Query("""
            UPDATE Borrow b
            SET b.status = :overdueStatus,
                b.overdueReminderSentDate = :processedOn
            WHERE b.borrowId = :borrowId
              AND b.status IN :eligibleStatuses
              AND b.dueDate < :today
              AND b.overdueReminderSentDate IS NULL
            """)
    int markOverdueReminderSent(@Param("borrowId") Integer borrowId,
                                @Param("eligibleStatuses") Collection<String> eligibleStatuses,
                                @Param("overdueStatus") String overdueStatus,
                                @Param("today") LocalDate today,
                                @Param("processedOn") LocalDate processedOn);

    @Query("SELECT FUNCTION('MONTH', b.borrowDate) AS m, COUNT(b) FROM Borrow b " +
           "WHERE b.borrowDate >= :startDate GROUP BY FUNCTION('MONTH', b.borrowDate) " +
           "ORDER BY FUNCTION('MONTH', b.borrowDate)")
    List<Object[]> countByMonthSince(@Param("startDate") java.time.LocalDate startDate);

    @Query("SELECT b.status, COUNT(b) FROM Borrow b GROUP BY b.status")
    List<Object[]> countByStatusGroup();
}
