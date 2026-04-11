package com.library.shared.realtime;

import com.library.domain.model.BookHold;
import com.library.domain.model.Borrow;
import com.library.domain.model.Orders;
import com.library.feature.borrow.BorrowQueryService;
import com.library.shared.constant.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminLiveUpdateService {

    private final AdminLiveEventBroker adminLiveEventBroker;
    private final BorrowQueryService borrowQueryService;

    public AdminLiveUpdateService(AdminLiveEventBroker adminLiveEventBroker,
                                  BorrowQueryService borrowQueryService) {
        this.adminLiveEventBroker = adminLiveEventBroker;
        this.borrowQueryService = borrowQueryService;
    }

    @Transactional(readOnly = true)
    public void publishBorrowRequested(Borrow borrow) {
        if (borrow == null || borrow.getBorrowId() == null) {
            return;
        }

        adminLiveEventBroker.broadcast(new AdminLiveEvent(
                "BORROW_REQUESTED",
                "Yêu cầu mượn mới",
                actorName(borrow.getStudent() != null ? borrow.getStudent().getStudentName() : null)
                        + " vừa gửi đơn mượn #"
                        + borrow.getBorrowId() + ".",
                "/admin/borrows",
                "warning",
                borrowQueryService.countPending(),
                LocalDateTime.now().toString()
        ));
    }

    @Transactional(readOnly = true)
    public void publishOrderCreated(Orders order) {
        if (order == null || order.getOrderId() == null) {
            return;
        }

        String normalizedStatus = OrderStatus.normalize(order.getStatus());
        String tone = OrderStatus.WAITING.equals(normalizedStatus) ? "warning" : "success";
        String suffix = OrderStatus.WAITING.equals(normalizedStatus)
                ? " và đang chờ đủ sách."
                : " và sẵn sàng để xử lý.";

        adminLiveEventBroker.broadcast(new AdminLiveEvent(
                "ORDER_CREATED",
                "Đơn mua mới",
                actorName(order.getStudent() != null ? order.getStudent().getStudentName() : null)
                        + " vừa tạo đơn mua #"
                        + order.getOrderId() + suffix,
                "/admin/orders",
                tone,
                borrowQueryService.countPending(),
                LocalDateTime.now().toString()
        ));
    }

    @Transactional(readOnly = true)
    public void publishHoldFulfilled(BookHold hold, Borrow autoBorrow) {
        if (hold == null || autoBorrow == null || autoBorrow.getBorrowId() == null) {
            return;
        }

        String bookName = hold.getBook() != null ? hold.getBook().getBookName() : "đầu sách";
        adminLiveEventBroker.broadcast(new AdminLiveEvent(
                "HOLD_FULFILLED",
                "Hold đã được xử lý",
                actorName(hold.getStudent() != null ? hold.getStudent().getStudentName() : null)
                        + " được gán mượn tự động cho sách "
                        + bookName + " qua phiếu #"
                        + autoBorrow.getBorrowId() + ".",
                "/admin/borrows",
                "info",
                borrowQueryService.countPending(),
                LocalDateTime.now().toString()
        ));
    }

    private String actorName(String value) {
        return (value == null || value.isBlank()) ? "Sinh viên" : value.trim();
    }
}
