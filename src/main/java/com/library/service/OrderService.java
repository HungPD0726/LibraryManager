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
import com.library.web.view.OrderItemView;
import com.library.web.view.OrderRowView;
import com.library.web.view.PriceDisplayView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BookRepository bookRepository;
    private final BookPriceRepository bookPriceRepository;
    private final PriceRepository priceRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;

    @Transactional(readOnly = true)
    public Page<OrderRowView> findAdminPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderId").descending());
        Page<Orders> orders = orderRepository.findAdminPage(pageable);
        List<OrderRowView> rows = orders.getContent().stream().map(this::toRowView).toList();
        return new PageImpl<>(rows, pageable, orders.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Optional<OrderRowView> findOrderView(Integer orderId) {
        return orderRepository.findById(orderId).map(this::toRowView);
    }

    @Transactional(readOnly = true)
    public List<OrderRowView> findByStudent(Integer studentId) {
        return orderRepository.findByStudentStudentIdOrderByOrderIdDesc(studentId).stream()
                .map(this::toRowView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PriceDisplayView> getSellableBooks() {
        List<PriceDisplayView> rows = new ArrayList<>();
        for (Book book : bookRepository.findAll(Sort.by(Sort.Direction.ASC, "bookName"))) {
            Price currentPrice = resolveCurrentPrice(book.getBookId());
            if (currentPrice == null) {
                continue;
            }
            rows.add(new PriceDisplayView(
                    book.getBookId(),
                    book.getBookName(),
                    book.getAvailable(),
                    currentPrice.getAmount(),
                    currentPrice.getCurrency(),
                    currentPrice.getNote()
            ));
        }
        return rows;
    }

    @Transactional
    public Orders createStudentOrder(Integer studentId, Integer staffId, Map<Integer, Integer> requestedItems) {
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new IllegalArgumentException("Danh sách mua đang trống.");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên."));
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản gửi yêu cầu."));

        Map<Book, Integer> booksWithQty = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean allAvailable = true;

        for (Map.Entry<Integer, Integer> entry : requestedItems.entrySet()) {
            Integer bookId = entry.getKey();
            Integer quantity = entry.getValue();
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Số lượng mua không hợp lệ.");
            }

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách #" + bookId + "."));
            Price price = resolveCurrentPrice(bookId);
            if (price == null) {
                throw new IllegalArgumentException("Sách " + book.getBookName() + " chưa có giá bán hiện tại.");
            }

            booksWithQty.put(book, quantity);
            total = total.add(price.getAmount().multiply(BigDecimal.valueOf(quantity)));
            if (book.getAvailable() == null || book.getAvailable() < quantity) {
                allAvailable = false;
            }
        }

        Orders order = new Orders();
        order.setStudent(student);
        order.setStaff(staff);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(total);
        order.setStatus(allAvailable ? OrderStatus.READY : OrderStatus.WAITING);
        Orders saved = orderRepository.save(order);

        for (Map.Entry<Book, Integer> entry : booksWithQty.entrySet()) {
            Price price = resolveCurrentPrice(entry.getKey().getBookId());
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(saved.getOrderId());
            detail.setBookId(entry.getKey().getBookId());
            detail.setQuantity(entry.getValue());
            detail.setUnitPrice(price.getAmount());
            orderDetailRepository.save(detail);
        }

        return saved;
    }

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
            Book book = bookRepository.findById(detail.getBookId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách #" + detail.getBookId() + "."));

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

    private Price resolveCurrentPrice(Integer bookId) {
        Optional<BookPrice> bookPrice = bookPriceRepository.findCurrentByBookId(bookId);
        return bookPrice.flatMap(value -> priceRepository.findById(value.getPriceId())).orElse(null);
    }

    private OrderRowView toRowView(Orders order) {
        List<OrderItemView> items = orderDetailRepository.findByOrderId(order.getOrderId()).stream()
                .map(detail -> {
                    Book book = bookRepository.findById(detail.getBookId()).orElse(null);
                    String bookName = book != null ? book.getBookName() : "Sách #" + detail.getBookId();
                    BigDecimal lineTotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
                    return new OrderItemView(detail.getBookId(), bookName, detail.getQuantity(), detail.getUnitPrice(), lineTotal);
                })
                .toList();

        String staffName = order.getStaff() != null ? order.getStaff().getStaffName() : null;
        String studentName = order.getStudent() != null ? order.getStudent().getStudentName() : "Sinh viên #" + order.getOrderId();

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
}
