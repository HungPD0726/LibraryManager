document.addEventListener("DOMContentLoaded", () => {
    const body = document.body;
    const endpointPath = body?.dataset?.adminLiveUrl;
    if (!body?.classList.contains("admin-shell") || !endpointPath) {
        return;
    }

    const toastStack = ensureToastStack();
    const countNode = document.querySelector("[data-pending-count]");
    const liveFeed = document.querySelector("[data-admin-live-feed]");
    const liveFeedEmpty = document.querySelector("[data-admin-live-empty]");
    const connectionBadge = document.querySelector("[data-admin-live-connection]");

    let reconnectTimer = null;
    let socket = null;

    const updateConnectionBadge = (state) => {
        if (!connectionBadge) {
            return;
        }

        connectionBadge.classList.remove("success", "warning", "danger", "secondary");
        if (state === "connected") {
            connectionBadge.classList.add("success");
            connectionBadge.textContent = "Realtime đang bật";
            return;
        }
        if (state === "connecting") {
            connectionBadge.classList.add("warning");
            connectionBadge.textContent = "Đang kết nối realtime";
            return;
        }
        connectionBadge.classList.add("danger");
        connectionBadge.textContent = "Mất kết nối realtime";
    };

    const connect = () => {
        if (socket && (socket.readyState === window.WebSocket.OPEN || socket.readyState === window.WebSocket.CONNECTING)) {
            return;
        }

        updateConnectionBadge("connecting");
        const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
        socket = new window.WebSocket(`${protocol}//${window.location.host}${endpointPath}`);

        socket.addEventListener("open", () => {
            updateConnectionBadge("connected");
        });

        socket.addEventListener("message", (event) => {
            try {
                const payload = JSON.parse(event.data);
                handleLiveEvent(payload);
            } catch (error) {
                console.debug("Cannot parse admin live payload.", error);
            }
        });

        socket.addEventListener("close", () => {
            updateConnectionBadge("disconnected");
            scheduleReconnect();
        });

        socket.addEventListener("error", () => {
            updateConnectionBadge("disconnected");
        });
    };

    const scheduleReconnect = () => {
        if (reconnectTimer) {
            return;
        }
        reconnectTimer = window.setTimeout(() => {
            reconnectTimer = null;
            connect();
        }, 3000);
    };

    const handleLiveEvent = (payload) => {
        if (!payload || !payload.type) {
            return;
        }

        if (countNode && Number.isFinite(Number(payload.pendingBorrowCount))) {
            countNode.textContent = String(payload.pendingBorrowCount);
            flashPendingMetric(countNode);
        }

        renderToast(payload, toastStack);
        renderLiveFeedItem(payload, liveFeed, liveFeedEmpty);
        document.dispatchEvent(new CustomEvent("library-manager:admin-live-event", { detail: payload }));
    };

    connect();

    document.addEventListener("visibilitychange", () => {
        if (document.visibilityState === "visible" && (!socket || socket.readyState === window.WebSocket.CLOSED)) {
            connect();
        }
    });
});

function ensureToastStack() {
    let stack = document.querySelector(".admin-live-toast-stack");
    if (stack) {
        return stack;
    }

    stack = document.createElement("div");
    stack.className = "admin-live-toast-stack";
    document.body.appendChild(stack);
    return stack;
}

function flashPendingMetric(countNode) {
    const pill = countNode.closest(".metric-pill");
    if (!pill) {
        return;
    }

    pill.classList.remove("is-live-highlight");
    window.requestAnimationFrame(() => pill.classList.add("is-live-highlight"));
    window.setTimeout(() => pill.classList.remove("is-live-highlight"), 1800);
}

function renderToast(payload, stack) {
    if (!stack) {
        return;
    }

    const toast = document.createElement("article");
    toast.className = `admin-live-toast tone-${payload.tone || "info"}`;

    const icon = document.createElement("span");
    icon.className = "admin-live-toast-icon";
    icon.innerHTML = iconMarkup(payload.type);

    const content = document.createElement("div");
    content.className = "admin-live-toast-copy";

    const title = document.createElement("strong");
    title.textContent = payload.title || "Cập nhật mới";

    const message = document.createElement("p");
    message.textContent = payload.message || "";

    content.append(title, message);

    if (payload.href) {
        const link = document.createElement("a");
        link.className = "admin-live-toast-link";
        link.href = payload.href;
        link.textContent = "Mở nhanh";
        content.appendChild(link);
    }

    toast.append(icon, content);
    stack.prepend(toast);

    while (stack.children.length > 4) {
        stack.lastElementChild?.remove();
    }

    window.setTimeout(() => {
        toast.classList.add("is-leaving");
        window.setTimeout(() => toast.remove(), 240);
    }, 4200);
}

function renderLiveFeedItem(payload, liveFeed, emptyNode) {
    if (!liveFeed) {
        return;
    }

    emptyNode?.setAttribute("hidden", "hidden");

    const item = document.createElement("li");
    item.className = "dashboard-live-item";

    const badge = document.createElement("span");
    badge.className = `status-pill ${payload.tone || "info"}`;
    badge.textContent = liveBadgeLabel(payload.type);

    const copy = document.createElement("div");
    copy.className = "dashboard-live-copy";

    const title = document.createElement("strong");
    title.textContent = payload.title || "Cập nhật mới";

    const message = document.createElement("p");
    message.textContent = payload.message || "";

    const meta = document.createElement("small");
    meta.className = "muted-text";
    meta.textContent = formatOccurredAt(payload.occurredAt);

    copy.append(title, message, meta);

    item.append(badge, copy);

    if (payload.href) {
        const action = document.createElement("a");
        action.className = "btn btn-ghost btn-sm";
        action.href = payload.href;
        action.textContent = "Xem";
        item.appendChild(action);
    }

    liveFeed.prepend(item);
    while (liveFeed.children.length > 6) {
        liveFeed.lastElementChild?.remove();
    }
}

function formatOccurredAt(rawValue) {
    if (!rawValue) {
        return "Vừa xong";
    }

    const date = new Date(rawValue);
    if (Number.isNaN(date.getTime())) {
        return "Vừa xong";
    }

    return new Intl.DateTimeFormat("vi-VN", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        day: "2-digit",
        month: "2-digit"
    }).format(date);
}

function liveBadgeLabel(type) {
    switch (type) {
        case "BORROW_REQUESTED":
            return "Mượn mới";
        case "ORDER_CREATED":
            return "Đơn mua";
        case "HOLD_FULFILLED":
            return "Hold";
        default:
            return "Live";
    }
}

function iconMarkup(type) {
    switch (type) {
        case "BORROW_REQUESTED":
            return '<i class="fa-solid fa-book-open-reader"></i>';
        case "ORDER_CREATED":
            return '<i class="fa-solid fa-basket-shopping"></i>';
        case "HOLD_FULFILLED":
            return '<i class="fa-solid fa-bolt"></i>';
        default:
            return '<i class="fa-solid fa-bell"></i>';
    }
}
