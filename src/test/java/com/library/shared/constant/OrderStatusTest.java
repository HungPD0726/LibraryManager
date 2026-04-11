package com.library.shared.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    void normalize_shouldHandleCanonicalLegacyAndMojibakeStatuses() {
        assertThat(OrderStatus.normalize(OrderStatus.READY)).isEqualTo(OrderStatus.READY);
        assertThat(OrderStatus.normalize(OrderStatus.DELIVERED)).isEqualTo(OrderStatus.DELIVERED);
        assertThat(OrderStatus.normalize("Pending")).isEqualTo(OrderStatus.LEGACY_PENDING);
        assertThat(OrderStatus.normalize("Approved")).isEqualTo(OrderStatus.LEGACY_APPROVED);
        assertThat(OrderStatus.normalize("Rejected")).isEqualTo(OrderStatus.LEGACY_REJECTED);
        assertThat(OrderStatus.normalize("SÃ¡ÂºÂµn sÃƒÂ ng")).isEqualTo(OrderStatus.READY);
        assertThat(OrderStatus.normalize("Ã„ÂÃƒÂ£ giao")).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void canBeDelivered_shouldAcceptSupportedOpenStates() {
        assertThat(OrderStatus.canBeDelivered(OrderStatus.READY)).isTrue();
        assertThat(OrderStatus.canBeDelivered(OrderStatus.WAITING)).isTrue();
        assertThat(OrderStatus.canBeDelivered("Pending")).isTrue();
        assertThat(OrderStatus.canBeDelivered("SÃ¡ÂºÂµn sÃƒÂ ng")).isTrue();
        assertThat(OrderStatus.canBeDelivered(OrderStatus.DELIVERED)).isFalse();
    }

    @Test
    void isClosed_shouldTreatResolvedAndDeliveredStatesAsClosed() {
        assertThat(OrderStatus.isClosed(OrderStatus.DELIVERED)).isTrue();
        assertThat(OrderStatus.isClosed(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.isClosed("Approved")).isTrue();
        assertThat(OrderStatus.isClosed("Rejected")).isTrue();
        assertThat(OrderStatus.isClosed(OrderStatus.READY)).isFalse();
        assertThat(OrderStatus.isClosed("Pending")).isFalse();
    }
}
