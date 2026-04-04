package com.library.feature.order;

import com.library.feature.catalog.PriceDisplayView;

import java.util.List;

public record StudentBuyPageView(
        List<PriceDisplayView> bookPrices,
        List<WaitlistItemView> waitlistItems,
        List<OrderRowView> orderHistory
) {
}
