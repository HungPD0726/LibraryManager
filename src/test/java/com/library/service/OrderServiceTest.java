package com.library.service;

import com.library.constant.OrderStatus;
import com.library.entity.Book;
import com.library.entity.BookPrice;
import com.library.entity.OrderDetail;
import com.library.entity.Orders;
import com.library.entity.Price;
import com.library.entity.Staff;
import com.library.entity.Student;
import com.library.repository.BookPriceRepository;
import com.library.repository.BookRepository;
import com.library.repository.OrderDetailRepository;
import com.library.repository.OrderRepository;
import com.library.repository.PriceRepository;
import com.library.repository.StaffRepository;
import com.library.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookPriceRepository bookPriceRepository;
    @Mock
    private PriceRepository priceRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createStudentOrder_shouldUseReadyStatusWhenStockIsEnough() {
        Student student = new Student();
        student.setStudentId(8);
        Staff staff = new Staff();
        staff.setStaffId(4);

        Book book = new Book();
        book.setBookId(12);
        book.setBookName("Spring in Action");
        book.setQuantity(10);
        book.setAvailable(6);

        BookPrice link = new BookPrice();
        link.setBookId(12);
        link.setPriceId(88);

        Price price = new Price();
        price.setPriceId(88);
        price.setAmount(new BigDecimal("250000"));
        price.setCurrency("VND");

        when(studentRepository.findById(8)).thenReturn(Optional.of(student));
        when(staffRepository.findById(4)).thenReturn(Optional.of(staff));
        when(bookRepository.findById(12)).thenReturn(Optional.of(book));
        when(bookPriceRepository.findCurrentByBookId(12)).thenReturn(Optional.of(link));
        when(priceRepository.findById(88)).thenReturn(Optional.of(price));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> {
            Orders order = invocation.getArgument(0);
            if (order.getOrderId() == null) {
                order.setOrderId(501);
            }
            return order;
        });

        Orders order = orderService.createStudentOrder(8, 4, Map.of(12, 2));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("500000");
        verify(bookRepository, never()).save(any(Book.class));

        ArgumentCaptor<OrderDetail> detailCaptor = ArgumentCaptor.forClass(OrderDetail.class);
        verify(orderDetailRepository).save(detailCaptor.capture());
        assertThat(detailCaptor.getValue().getQuantity()).isEqualTo(2);
        assertThat(detailCaptor.getValue().getUnitPrice()).isEqualByComparingTo("250000");
    }

    @Test
    void createStudentOrder_shouldUseWaitingStatusWhenStockIsMissing() {
        Student student = new Student();
        student.setStudentId(8);
        Staff staff = new Staff();
        staff.setStaffId(4);

        Book book = new Book();
        book.setBookId(13);
        book.setBookName("Microservices Patterns");
        book.setQuantity(3);
        book.setAvailable(0);

        BookPrice link = new BookPrice();
        link.setBookId(13);
        link.setPriceId(89);

        Price price = new Price();
        price.setPriceId(89);
        price.setAmount(new BigDecimal("300000"));
        price.setCurrency("VND");

        when(studentRepository.findById(8)).thenReturn(Optional.of(student));
        when(staffRepository.findById(4)).thenReturn(Optional.of(staff));
        when(bookRepository.findById(13)).thenReturn(Optional.of(book));
        when(bookPriceRepository.findCurrentByBookId(13)).thenReturn(Optional.of(link));
        when(priceRepository.findById(89)).thenReturn(Optional.of(price));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> {
            Orders order = invocation.getArgument(0);
            order.setOrderId(502);
            return order;
        });

        Orders order = orderService.createStudentOrder(8, 4, Map.of(13, 1));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.WAITING);
    }

    @Test
    void completeDelivery_shouldDecreaseQuantityAndAvailableOnlyWhenDelivering() {
        Orders order = new Orders();
        order.setOrderId(601);
        order.setStatus(OrderStatus.READY);

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(601);
        detail.setBookId(20);
        detail.setQuantity(2);
        detail.setUnitPrice(new BigDecimal("150000"));

        Book book = new Book();
        book.setBookId(20);
        book.setBookName("Effective Java");
        book.setQuantity(5);
        book.setAvailable(4);

        Staff staff = new Staff();
        staff.setStaffId(9);

        when(orderRepository.findById(601)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrderId(601)).thenReturn(List.of(detail));
        when(bookRepository.findById(20)).thenReturn(Optional.of(book));
        when(staffRepository.findById(9)).thenReturn(Optional.of(staff));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Orders delivered = orderService.completeDelivery(601, 9);

        assertThat(delivered.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(delivered.getStaff()).isEqualTo(staff);
        assertThat(book.getQuantity()).isEqualTo(3);
        assertThat(book.getAvailable()).isEqualTo(2);
        verify(bookRepository).save(book);
    }
}
