package com.library.feature.borrow;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PendingCountController {

    private final BorrowQueryService borrowQueryService;

    @GetMapping("/api/pending-count")
    public Map<String, Long> pendingCount() {
        return Map.of("pendingCount", borrowQueryService.countPending());
    }
}
