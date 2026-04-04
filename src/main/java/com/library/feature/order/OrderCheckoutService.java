package com.library.feature.order;

import com.library.domain.model.Book;
import com.library.domain.model.OrderDetail;
import com.library.domain.model.Orders;
import com.library.domain.model.Price;
import com.library.domain.model.Staff;
import com.library.domain.model.Student;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.OrderDetailRepository;
import com.library.domain.repository.OrderRepository;
import com.library.domain.repository.StaffRepository;
import com.library.domain.repository.StudentRepository;
import com.library.shared.constant.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderCheckoutService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final BookPricingService bookPricingService;

    @Transactional
    public Orders createStudentOrder(Integer studentId, Integer staffId, Map<Integer, Integer> requestedItems) {
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new IllegalArgumentException("Danh sách mua đang trống.");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên."));
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản gửi yêu cầu."));

        Map<Integer, Book> booksById = new LinkedHashMap<>();
        for (Book book : bookRepository.findAllById(requestedItems.keySet())) {
            booksById.put(book.getBookId(), book);
        }
        Map<Integer, Price> pricesByBookId = bookPricingService.findCurrentPrices(requestedItems.keySet());

        BigDecimal total = BigDecimal.ZERO;
        boolean allAvailable = true;
        List<OrderDetail> details = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : requestedItems.entrySet()) {
            Integer bookId = entry.getKey();
            Integer quantity = entry.getValue();
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Số lượng mua không hợp lệ.");
            }

            Book book = booksById.get(bookId);
            if (book == null) {
                throw new IllegalArgumentException("Không tìm thấy sách #" + bookId + ".");
            }
            Price price = pricesByBookId.get(bookId);
            if (price == null) {
                throw new IllegalArgumentException("Sách " + book.getBookName() + " chưa có giá bán hiện tại.");
            }

            total = total.add(price.getAmount().multiply(BigDecimal.valueOf(quantity)));
            if (book.getAvailable() == null || book.getAvailable() < quantity) {
                allAvailable = false;
            }

            OrderDetail detail = new OrderDetail();
            detail.setBookId(bookId);
            detail.setQuantity(quantity);
            detail.setUnitPrice(price.getAmount());
            details.add(detail);
        }

        Orders order = new Orders();
        order.setStudent(student);
        order.setStaff(staff);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(total);
        order.setStatus(allAvailable ? OrderStatus.READY : OrderStatus.WAITING);

        Orders saved = orderRepository.save(order);
        for (OrderDetail detail : details) {
            detail.setOrderId(saved.getOrderId());
            orderDetailRepository.save(detail);
        }
        return saved;
    }
}
