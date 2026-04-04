package com.library.feature.order;

import com.library.domain.model.Book;
import com.library.domain.model.OrderDetail;
import com.library.domain.model.Orders;
import com.library.domain.repository.OrderDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderViewAssembler {

    private final OrderDetailRepository orderDetailRepository;

    public Page<OrderRowView> toPage(Page<Orders> orders) {
        return new PageImpl<>(
                toViews(orders.getContent()),
                orders.getPageable(),
                orders.getTotalElements()
        );
    }

    public List<OrderRowView> toViews(List<Orders> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }

        List<Integer> orderIds = orders.stream()
                .map(Orders::getOrderId)
                .toList();

        Map<Integer, List<OrderItemView>> itemsByOrderId = new LinkedHashMap<>();
        for (OrderDetail detail : orderDetailRepository.findByOrderIdIn(orderIds)) {
            itemsByOrderId.computeIfAbsent(detail.getOrderId(), ignored -> new java.util.ArrayList<>())
                    .add(toItemView(detail));
        }

        return orders.stream()
                .map(order -> toView(order, itemsByOrderId.getOrDefault(order.getOrderId(), List.of())))
                .toList();
    }

    public OrderRowView toView(Orders order) {
        List<OrderItemView> items = orderDetailRepository.findByOrderId(order.getOrderId()).stream()
                .map(this::toItemView)
                .toList();
        return toView(order, items);
    }

    private OrderRowView toView(Orders order, List<OrderItemView> items) {
        String staffName = order.getStaff() != null ? order.getStaff().getStaffName() : null;
        String studentName = order.getStudent() != null
                ? order.getStudent().getStudentName()
                : "Sinh viên #" + order.getOrderId();

        return new OrderRowView(
                order.getOrderId(),
                studentName,
                staffName,
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                items
        );
    }

    private OrderItemView toItemView(OrderDetail detail) {
        Book book = detail.getBook();
        String bookName = book != null ? book.getBookName() : "Sách #" + detail.getBookId();
        BigDecimal lineTotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
        return new OrderItemView(detail.getBookId(), bookName, detail.getQuantity(), detail.getUnitPrice(), lineTotal);
    }
}
