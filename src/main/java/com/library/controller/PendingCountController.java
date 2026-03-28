package com.library.controller;

import com.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PendingCountController {

    private final BorrowService borrowService;

    @GetMapping("/api/pending-count")
    public Map<String, Long> pendingCount() {
        return Map.of("pendingCount", borrowService.countPending());
    }
}
