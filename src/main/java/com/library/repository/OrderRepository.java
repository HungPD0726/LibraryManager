package com.library.repository;

import com.library.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Integer> {
    Page<Orders> findByStudentStudentId(Integer studentId, Pageable pageable);
    List<Orders> findByStudentStudentIdOrderByOrderIdDesc(Integer studentId);
    long countByStatus(String status);

    @Query("SELECT o FROM Orders o ORDER BY o.orderId DESC")
    Page<Orders> findAdminPage(Pageable pageable);
}
