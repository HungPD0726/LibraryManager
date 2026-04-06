document.addEventListener("DOMContentLoaded", () => {
    bindChatbot();
});

function bindChatbot() {
    const root = document.getElementById("chatbotApp");
    const form = document.getElementById("chatbotForm");
    const input = document.getElementById("chatbotInput");
    const log = document.getElementById("chatbotMessages");
    const statusBadge = document.getElementById("chatbotStatusBadge");
    const statusText = document.getElementById("chatbotStatusText");
    const promptButtons = document.querySelectorAll("[data-chatbot-prompt]");
    const clearButtons = document.querySelectorAll("[data-chatbot-clear]");

    if (!root || !form || !input || !log) {
        return;
    }

    const endpoint = form.dataset.endpoint || root.dataset.endpoint || window.location.pathname;
    const viewerName = root.dataset.viewer || "Bạn";
    const assistantName = "Library AI";
    const storageKey = root.dataset.storageKey || "library-manager.chatbot";
    const initialConversation = readConversationFromDom(log);
    let conversation = loadConversation(storageKey, initialConversation);

    renderConversation(log, conversation, viewerName, assistantName);
    setChatbotStatus(
        statusBadge,
        statusText,
        "success",
        "Sẵn sàng",
        "Assistant sẽ trả lời ngắn gọn, bằng tiếng Việt và ưu tiên hướng dẫn thực tế."
    );

    promptButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const prompt = button.dataset.chatbotPrompt ? button.dataset.chatbotPrompt.trim() : "";
            if (!prompt) {
                return;
            }

            input.value = prompt;
            input.focus();
            input.setSelectionRange(input.value.length, input.value.length);
            setChatbotStatus(
                statusBadge,
                statusText,
                "info",
                "Đã nạp gợi ý",
                "Bạn có thể chỉnh lại câu hỏi trước khi gửi."
            );
        });
    });

    clearButtons.forEach((button) => {
        button.addEventListener("click", () => {
            conversation = cloneConversation(initialConversation);
            persistConversation(storageKey, conversation);
            renderConversation(log, conversation, viewerName, assistantName);
            setChatbotStatus(
                statusBadge,
                statusText,
                "info",
                "Phiên mới",
                "Đã làm mới hội thoại. Bạn có thể bắt đầu với một câu hỏi khác."
            );
            input.focus();
        });
    });

    input.addEventListener("keydown", (event) => {
        if (event.key === "Enter" && !event.shiftKey) {
            event.preventDefault();
            form.requestSubmit();
        }
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const question = input.value.trim();
        if (!question) {
            setChatbotStatus(
                statusBadge,
                statusText,
                "warning",
                "Thiếu nội dung",
                "Hãy nhập một câu hỏi cụ thể để assistant có thể hỗ trợ tốt hơn."
            );
            input.focus();
            return;
        }

        const userMessage = {
            role: "user",
            content: question,
            timestamp: Date.now()
        };

        conversation.push(userMessage);
        conversation = trimConversation(conversation);
        persistConversation(storageKey, conversation);
        appendMessage(log, userMessage, viewerName, assistantName);
        input.value = "";

        setFormPendingState(form, input, promptButtons, clearButtons, true);
        const typingIndicator = appendTypingIndicator(log, assistantName);
        setChatbotStatus(
            statusBadge,
            statusText,
            "info",
            "Đang phản hồi",
            "Assistant đang phân tích câu hỏi và soạn câu trả lời."
        );

        try {
            const response = await fetch(endpoint, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    messages: conversation.map((message) => ({
                        role: message.role,
                        content: message.content
                    }))
                })
            });

            const payload = await readJson(response);
            if (!response.ok) {
                throw new Error(payload.error || "Không thể nhận phản hồi từ chatbot.");
            }

            typingIndicator.remove();
            const assistantMessage = {
                role: "assistant",
                content: payload.reply,
                timestamp: Date.now()
            };
            conversation.push(assistantMessage);
            conversation = trimConversation(conversation);
            persistConversation(storageKey, conversation);
            appendMessage(log, assistantMessage, viewerName, assistantName);
            setChatbotStatus(
                statusBadge,
                statusText,
                "success",
                "Đã nhận phản hồi",
                payload.model
                    ? `Phản hồi vừa được tạo bởi model ${payload.model}.`
                    : "Assistant đã trả lời xong."
            );
        } catch (error) {
            typingIndicator.remove();
            appendMessage(
                log,
                {
                    role: "assistant",
                    content: error.message || "Chatbot đang tạm bận, vui lòng thử lại sau.",
                    timestamp: Date.now()
                },
                viewerName,
                assistantName,
                { isTransient: true }
            );
            setChatbotStatus(
                statusBadge,
                statusText,
                "danger",
                "Có lỗi xảy ra",
                error.message || "Chatbot đang tạm bận, vui lòng thử lại sau."
            );
        } finally {
            setFormPendingState(form, input, promptButtons, clearButtons, false);
            input.focus();
        }
    });
}

function readConversationFromDom(log) {
    return Array.from(log.querySelectorAll(".chat-message"))
        .map((node) => {
            const paragraph = node.querySelector(".chat-bubble p");
            if (!paragraph || !paragraph.textContent.trim()) {
                return null;
            }

            return {
                role: node.classList.contains("assistant") ? "assistant" : "user",
                content: paragraph.textContent.trim(),
                timestamp: Date.now()
            };
        })
        .filter(Boolean);
}

function renderConversation(log, conversation, viewerName, assistantName) {
    log.innerHTML = "";
    conversation.forEach((message) => appendMessage(log, message, viewerName, assistantName));
    log.scrollTop = log.scrollHeight;
}

function appendMessage(log, message, viewerName, assistantName, options = {}) {
    const container = document.createElement("article");
    container.className = `chat-message ${message.role}`;
    if (options.isTransient) {
        container.dataset.transient = "true";
    }

    const avatar = document.createElement("div");
    avatar.className = "chat-avatar";
    avatar.innerHTML = message.role === "assistant"
        ? '<i class="fa-solid fa-robot"></i>'
        : '<i class="fa-solid fa-user"></i>';

    const bubble = document.createElement("div");
    bubble.className = `chat-bubble ${message.role}`;

    const meta = document.createElement("div");
    meta.className = "chat-meta";

    const author = document.createElement("strong");
    author.textContent = message.role === "assistant" ? assistantName : viewerName;

    const time = document.createElement("span");
    time.textContent = formatChatTime(message.timestamp);

    meta.append(author, time);

    const paragraph = document.createElement("p");
    paragraph.textContent = message.content;

    bubble.append(meta, paragraph);
    container.append(avatar, bubble);
    log.appendChild(container);
    log.scrollTop = log.scrollHeight;
}

function appendTypingIndicator(log, assistantName) {
    const container = document.createElement("article");
    container.className = "chat-message assistant is-typing";

    const avatar = document.createElement("div");
    avatar.className = "chat-avatar";
    avatar.innerHTML = '<i class="fa-solid fa-robot"></i>';

    const bubble = document.createElement("div");
    bubble.className = "chat-bubble assistant";

    const meta = document.createElement("div");
    meta.className = "chat-meta";

    const author = document.createElement("strong");
    author.textContent = assistantName;

    const time = document.createElement("span");
    time.textContent = "Đang soạn";

    meta.append(author, time);

    const typingDots = document.createElement("div");
    typingDots.className = "typing-dots";
    typingDots.innerHTML = "<span></span><span></span><span></span>";

    bubble.append(meta, typingDots);
    container.append(avatar, bubble);
    log.appendChild(container);
    log.scrollTop = log.scrollHeight;
    return container;
}

function setFormPendingState(form, input, promptButtons, clearButtons, pending) {
    const submitButton = form.querySelector("button[type='submit']");
    if (submitButton) {
        submitButton.disabled = pending;
    }

    input.readOnly = pending;
    promptButtons.forEach((button) => {
        button.disabled = pending;
    });
    clearButtons.forEach((button) => {
        button.disabled = pending;
    });
}

function setChatbotStatus(badge, text, tone, badgeText, helperText) {
    const normalizedTone = ["success", "warning", "danger", "info", "secondary"].includes(tone)
        ? tone
        : "secondary";

    if (badge) {
        badge.className = `status-pill ${normalizedTone}`;
        badge.textContent = badgeText;
    }

    if (text) {
        text.textContent = helperText;
        text.classList.toggle("is-error", normalizedTone === "danger");
    }
}

function persistConversation(storageKey, conversation) {
    try {
        window.localStorage.setItem(storageKey, JSON.stringify(trimConversation(conversation)));
    } catch (error) {
        console.warn("Không thể lưu hội thoại chatbot.", error);
    }
}

function loadConversation(storageKey, fallbackConversation) {
    try {
        const raw = window.localStorage.getItem(storageKey);
        if (!raw) {
            return cloneConversation(fallbackConversation);
        }

        const parsed = JSON.parse(raw);
        if (!Array.isArray(parsed)) {
            return cloneConversation(fallbackConversation);
        }

        const normalized = parsed
            .filter((message) => message && typeof message.content === "string")
            .map((message) => ({
                role: message.role === "assistant" ? "assistant" : "user",
                content: message.content.trim(),
                timestamp: Number.isFinite(message.timestamp) ? message.timestamp : Date.now()
            }))
            .filter((message) => message.content.length > 0);

        return normalized.length ? trimConversation(normalized) : cloneConversation(fallbackConversation);
    } catch (error) {
        console.warn("Không thể đọc hội thoại chatbot đã lưu.", error);
        return cloneConversation(fallbackConversation);
    }
}

function trimConversation(conversation) {
    return conversation.slice(-24);
}

function cloneConversation(conversation) {
    return conversation.map((message) => ({ ...message }));
}

async function readJson(response) {
    const raw = await response.text();
    if (!raw) {
        return {};
    }

    try {
        return JSON.parse(raw);
    } catch (error) {
        return { error: raw };
    }
}

function formatChatTime(timestamp) {
    const value = Number.isFinite(timestamp) ? timestamp : Date.now();
    return new Intl.DateTimeFormat("vi-VN", {
        hour: "2-digit",
        minute: "2-digit"
    }).format(new Date(value));
}
