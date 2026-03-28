package com.library.repository;

import com.library.entity.BorrowItem;
import com.library.entity.BorrowItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BorrowItemRepository extends JpaRepository<BorrowItem, BorrowItemId> {
    List<BorrowItem> findByBorrowId(Integer borrowId);
}
