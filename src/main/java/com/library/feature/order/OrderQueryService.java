package com.library.feature.order;

import com.library.domain.model.Orders;
import com.library.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderViewAssembler orderViewAssembler;

    @Transactional(readOnly = true)
    public Page<OrderRowView> findAdminPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderId").descending());
        return orderViewAssembler.toPage(orderRepository.findAdminPage(pageable));
    }

    @Transactional(readOnly = true)
    public Optional<OrderRowView> findOrderView(Integer orderId) {
        return orderRepository.findById(orderId).map(orderViewAssembler::toView);
    }

    @Transactional(readOnly = true)
    public List<OrderRowView> findByStudent(Integer studentId) {
        return orderViewAssembler.toViews(orderRepository.findByStudentStudentIdOrderByOrderIdDesc(studentId));
    }

    @Transactional(readOnly = true)
    public long countByStudent(Integer studentId) {
        return orderRepository.countByStudentStudentId(studentId);
    }
}
