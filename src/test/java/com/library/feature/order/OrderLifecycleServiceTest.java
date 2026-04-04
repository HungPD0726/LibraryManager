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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderLifecycleServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private OrderLifecycleService orderLifecycleService;

    @Test
    void completeDelivery_shouldDecreaseQuantityAndAvailableOnlyWhenDelivering() {
        Orders order = new Orders();
        order.setOrderId(601);
        order.setStatus(OrderStatus.READY);

        Book book = new Book();
        book.setBookId(20);
        book.setBookName("Effective Java");
        book.setQuantity(5);
        book.setAvailable(4);

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(601);
        detail.setBookId(20);
        detail.setQuantity(2);
        detail.setBook(book);

        Staff staff = new Staff();
        staff.setStaffId(9);

        when(orderRepository.findById(601)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrderId(601)).thenReturn(List.of(detail));
        when(staffRepository.findById(9)).thenReturn(Optional.of(staff));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Orders delivered = orderLifecycleService.completeDelivery(601, 9);

        assertThat(delivered.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(delivered.getStaff()).isEqualTo(staff);
        assertThat(book.getQuantity()).isEqualTo(3);
        assertThat(book.getAvailable()).isEqualTo(2);
        verify(bookRepository).save(book);
    }

    @Test
    void cancelOrder_shouldMarkAsCancelledWithoutTouchingInventory() {
        Orders order = new Orders();
        order.setOrderId(602);
        order.setStatus(OrderStatus.WAITING);

        Staff staff = new Staff();
        staff.setStaffId(10);

        when(orderRepository.findById(602)).thenReturn(Optional.of(order));
        when(staffRepository.findById(10)).thenReturn(Optional.of(staff));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Orders cancelled = orderLifecycleService.cancelOrder(602, 10);

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.getStaff()).isEqualTo(staff);
        verify(bookRepository, never()).save(any(Book.class));
    }
}
