package com.library.domain.repository;

import com.library.domain.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Integer> {

    @EntityGraph(attributePaths = {"student", "staff"})
    @Override
    java.util.Optional<Orders> findById(Integer id);

    Page<Orders> findByStudentStudentId(Integer studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "staff"})
    List<Orders> findByStudentStudentIdOrderByOrderIdDesc(Integer studentId);

    long countByStudentStudentId(Integer studentId);

    long countByStatus(String status);

    @EntityGraph(attributePaths = {"student", "staff"})
    @Query("SELECT o FROM Orders o")
    Page<Orders> findAdminPage(Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Orders o WHERE o.status = :status")
    java.math.BigDecimal sumRevenueByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Orders o WHERE o.status IN :statuses")
    java.math.BigDecimal sumRevenueByStatuses(@Param("statuses") Collection<String> statuses);

    @Query("""
            SELECT YEAR(o.orderDate), MONTH(o.orderDate), COALESCE(SUM(o.totalAmount), 0)
            FROM Orders o
            WHERE o.status = :status AND o.orderDate >= :startDate
            GROUP BY YEAR(o.orderDate), MONTH(o.orderDate)
            ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)
            """)
    List<Object[]> sumRevenueByMonthSince(@Param("status") String status, @Param("startDate") LocalDate startDate);

    @Query("""
            SELECT YEAR(o.orderDate), MONTH(o.orderDate), COALESCE(SUM(o.totalAmount), 0)
            FROM Orders o
            WHERE o.status IN :statuses AND o.orderDate >= :startDate
            GROUP BY YEAR(o.orderDate), MONTH(o.orderDate)
            ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)
            """)
    List<Object[]> sumRevenueByMonthSinceStatuses(@Param("statuses") Collection<String> statuses, @Param("startDate") LocalDate startDate);
}
