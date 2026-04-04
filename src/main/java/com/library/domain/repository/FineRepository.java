package com.library.domain.repository;

import com.library.domain.model.Fine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Integer> {

    List<Fine> findByBorrowBorrowId(Integer borrowId);

    List<Fine> findByStatus(String status);

    long countByStatus(String status);

    @Query("SELECT f FROM Fine f ORDER BY f.fineId DESC")
    Page<Fine> findAllPaged(Pageable pageable);

    @Query("SELECT f FROM Fine f WHERE f.status = :status ORDER BY f.fineId DESC")
    Page<Fine> findByStatusPaged(@Param("status") String status, Pageable pageable);

    @Query("SELECT f FROM Fine f WHERE f.borrow.student.studentId = :studentId ORDER BY f.fineId DESC")
    List<Fine> findByStudentId(@Param("studentId") Integer studentId);

    @Query("SELECT f FROM Fine f WHERE f.borrow.student.studentId = :studentId AND f.status = :status")
    List<Fine> findByStudentIdAndStatus(@Param("studentId") Integer studentId, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.status = 'Unpaid'")
    BigDecimal sumUnpaidAmount();

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.status = 'Paid'")
    BigDecimal sumPaidAmount();

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.borrow.student.studentId = :studentId AND f.status = 'Unpaid'")
    BigDecimal sumUnpaidByStudentId(@Param("studentId") Integer studentId);

    boolean existsByBorrowBorrowId(Integer borrowId);
}
