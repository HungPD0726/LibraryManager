package com.library.feature.order;

import com.library.domain.model.Book;
import com.library.domain.model.OrderDetail;
import com.library.domain.model.Orders;
import com.library.domain.model.Staff;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.OrderDetailRepository;
import com.library.domain.repository.OrderRepository;
import com.library.domain.repository.StaffRepository;
import com.library.shared.constant.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderLifecycleService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BookRepository bookRepository;
    private final StaffRepository staffRepository;

    @Transactional
    public Orders completeDelivery(Integer orderId, Integer staffId) {
        Orders order = getEditableOrder(orderId);
        if (!OrderStatus.canBeDelivered(order.getStatus())) {
            throw new IllegalArgumentException("Đơn hàng hiện tại không ở trạng thái có thể giao.");
        }

        List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
        if (details.isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng không có chi tiết sách.");
        }

        for (OrderDetail detail : details) {
            Book book = detail.getBook();
            if (book == null) {
                book = bookRepository.findById(detail.getBookId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách #" + detail.getBookId() + "."));
            }

            if (book.getQuantity() < detail.getQuantity() || book.getAvailable() < detail.getQuantity()) {
                throw new IllegalArgumentException("Sách " + book.getBookName() + " không đủ tồn kho để giao.");
            }

            book.setQuantity(book.getQuantity() - detail.getQuantity());
            book.setAvailable(book.getAvailable() - detail.getQuantity());
            bookRepository.save(book);
        }

        order.setStaff(resolveStaff(staffId));
        order.setStatus(OrderStatus.DELIVERED);
        return orderRepository.save(order);
    }

    @Transactional
    public Orders cancelOrder(Integer orderId, Integer staffId) {
        Orders order = getEditableOrder(orderId);
        if (OrderStatus.isClosed(order.getStatus())) {
            throw new IllegalArgumentException("Đơn hàng này đã được xử lý trước đó.");
        }
        order.setStaff(resolveStaff(staffId));
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Transactional
    public Orders approveLegacyPending(Integer orderId, Integer staffId) {
        Orders order = getEditableOrder(orderId);
        if (!OrderStatus.LEGACY_PENDING.equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể duyệt đơn đang ở trạng thái Pending.");
        }
        order.setStaff(resolveStaff(staffId));
        order.setStatus(OrderStatus.READY);
        return orderRepository.save(order);
    }

    @Transactional
    public Orders rejectLegacyPending(Integer orderId, Integer staffId) {
        Orders order = getEditableOrder(orderId);
        if (!OrderStatus.LEGACY_PENDING.equals(order.getStatus())) {
            throw new IllegalArgumentException("Chỉ có thể từ chối đơn đang ở trạng thái Pending.");
        }
        order.setStaff(resolveStaff(staffId));
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    private Orders getEditableOrder(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
    }

    private Staff resolveStaff(Integer staffId) {
        return staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên xử lý."));
    }
}
