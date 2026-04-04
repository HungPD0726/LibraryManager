package com.library.domain.repository;

import com.library.domain.model.OrderDetail;
import com.library.domain.model.OrderDetailId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId> {

    @EntityGraph(attributePaths = {"book"})
    List<OrderDetail> findByOrderId(Integer orderId);

    @EntityGraph(attributePaths = {"book"})
    List<OrderDetail> findByOrderIdIn(List<Integer> orderIds);
}
