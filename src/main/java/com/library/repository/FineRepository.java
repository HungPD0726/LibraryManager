package com.library.repository;

import com.library.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Integer> {
    List<Fine> findByBorrowBorrowId(Integer borrowId);
    List<Fine> findByStatus(String status);
    long countByStatus(String status);
}
