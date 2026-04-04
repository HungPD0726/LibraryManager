package com.library.feature.order;

import com.library.domain.model.Book;
import com.library.domain.model.BookPrice;
import com.library.domain.model.OrderDetail;
import com.library.domain.model.Orders;
import com.library.domain.model.Price;
import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.BookPriceRepository;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.OrderDetailRepository;
import com.library.domain.repository.OrderRepository;
import com.library.domain.repository.StaffRepository;
import com.library.domain.repository.StudentRepository;
import com.library.shared.constant.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCheckoutServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private BookPriceRepository bookPriceRepository;

    private OrderCheckoutService orderCheckoutService;

    @BeforeEach
    void setUp() {
        BookPricingService bookPricingService = new BookPricingService(bookPriceRepository);
        orderCheckoutService = new OrderCheckoutService(
                orderRepository,
                orderDetailRepository,
                bookRepository,
                studentRepository,
                staffRepository,
                bookPricingService
        );
    }

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

        Price price = new Price();
        price.setPriceId(88);
        price.setAmount(new BigDecimal("250000"));
        price.setCurrency("VND");

        when(studentRepository.findById(8)).thenReturn(Optional.of(student));
        when(staffRepository.findById(4)).thenReturn(Optional.of(staff));
        BookPrice bookPrice = new BookPrice();
        bookPrice.setBookId(12);
        bookPrice.setPriceId(88);
        bookPrice.setStartDate(LocalDate.now());
        bookPrice.setPrice(price);

        when(bookRepository.findAllById(anyCollection())).thenReturn(List.of(book));
        when(bookPriceRepository.findCurrentByBookIds(anyCollection())).thenReturn(List.of(bookPrice));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> {
            Orders order = invocation.getArgument(0);
            order.setOrderId(501);
            return order;
        });

        Orders order = orderCheckoutService.createStudentOrder(8, 4, Map.of(12, 2));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("500000");

        ArgumentCaptor<OrderDetail> detailCaptor = ArgumentCaptor.forClass(OrderDetail.class);
        verify(orderDetailRepository).save(detailCaptor.capture());
        assertThat(detailCaptor.getValue().getOrderId()).isEqualTo(501);
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

        Price price = new Price();
        price.setPriceId(89);
        price.setAmount(new BigDecimal("300000"));
        price.setCurrency("VND");

        when(studentRepository.findById(8)).thenReturn(Optional.of(student));
        when(staffRepository.findById(4)).thenReturn(Optional.of(staff));
        BookPrice bookPrice = new BookPrice();
        bookPrice.setBookId(13);
        bookPrice.setPriceId(89);
        bookPrice.setStartDate(LocalDate.now());
        bookPrice.setPrice(price);

        when(bookRepository.findAllById(anyCollection())).thenReturn(List.of(book));
        when(bookPriceRepository.findCurrentByBookIds(anyCollection())).thenReturn(List.of(bookPrice));
        when(orderRepository.save(any(Orders.class))).thenAnswer(invocation -> {
            Orders order = invocation.getArgument(0);
            order.setOrderId(502);
            return order;
        });

        Orders order = orderCheckoutService.createStudentOrder(8, 4, Map.of(13, 1));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.WAITING);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("300000");
    }
}
