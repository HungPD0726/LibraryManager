document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-cover-picker]").forEach(bindBookCoverPicker);
});

function bindBookCoverPicker(picker) {
    const endpoint = picker.dataset.coverSuggestionsUrl;
    const fallbackUrl = picker.dataset.coverFallbackUrl || "https://placehold.co/240x320/e2f3f2/0f3d3b?text=Book";
    const titleInput = picker.querySelector("[data-cover-title-input]");
    const authorSelect = picker.querySelector("[data-cover-author-select]");
    const urlInput = picker.querySelector("[data-cover-url-input]");
    const preview = picker.querySelector("[data-cover-preview]");
    const searchButton = picker.querySelector("[data-cover-search]");
    const status = picker.querySelector("[data-cover-status]");
    const suggestionsPanel = picker.querySelector("[data-cover-suggestions]");

    if (!endpoint || !titleInput || !urlInput || !preview || !searchButton || !suggestionsPanel) {
        return;
    }

    bindPreviewFallback(preview, fallbackUrl);
    urlInput.addEventListener("input", () => {
        syncPreview(preview, urlInput.value, fallbackUrl);
        setStatus(status, "URL ảnh bìa đã được cập nhật.", "neutral");
    });

    searchButton.addEventListener("click", async () => {
        const title = titleInput.value.trim();
        if (!title) {
            setStatus(status, "Nhập tên sách trước khi tìm ảnh bìa.", "warning");
            titleInput.focus();
            return;
        }

        const author = selectedAuthorText(authorSelect);
        const originalButtonHtml = searchButton.innerHTML;
        searchButton.disabled = true;
        searchButton.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang tìm';
        setStatus(status, "Đang tìm ảnh bìa trên Open Library...", "neutral");

        try {
            const url = new URL(endpoint, window.location.origin);
            url.searchParams.set("title", title);
            if (author) {
                url.searchParams.set("author", author);
            }

            const response = await fetch(url, {
                headers: {
                    Accept: "application/json"
                },
                cache: "no-store",
                credentials: "same-origin"
            });

            if (!response.ok) {
                throw new Error(`Unexpected status ${response.status}`);
            }

            const suggestions = await response.json();
            renderSuggestions(suggestionsPanel, suggestions, {
                fallbackUrl,
                preview,
                status,
                urlInput
            });

            if (Array.isArray(suggestions) && suggestions.length > 0) {
                setStatus(status, `Tìm thấy ${suggestions.length} ảnh bìa phù hợp.`, "success");
                return;
            }
            setStatus(status, "Không tìm thấy ảnh bìa phù hợp. Bạn vẫn có thể dán URL thủ công.", "warning");
        } catch (error) {
            console.debug("Không thể tải gợi ý ảnh bìa.", error);
            renderSuggestions(suggestionsPanel, [], {
                fallbackUrl,
                preview,
                status,
                urlInput
            });
            setStatus(status, "Không thể tải gợi ý ảnh bìa. Bạn vẫn có thể dán URL thủ công.", "warning");
        } finally {
            searchButton.disabled = false;
            searchButton.innerHTML = originalButtonHtml;
        }
    });
}

function renderSuggestions(panel, suggestions, context) {
    panel.replaceChildren();
    panel.hidden = false;

    if (!Array.isArray(suggestions) || suggestions.length === 0) {
        const empty = document.createElement("div");
        empty.className = "cover-suggestion-empty";
        empty.innerHTML = '<i class="fa-regular fa-image"></i><span>Chưa có ảnh gợi ý cho đầu sách này.</span>';
        panel.append(empty);
        return;
    }

    const grid = document.createElement("div");
    grid.className = "cover-suggestion-grid";
    suggestions.forEach((suggestion) => {
        grid.append(createSuggestionCard(suggestion, context));
    });
    panel.append(grid);
}

function createSuggestionCard(suggestion, context) {
    const card = document.createElement("article");
    card.className = "cover-suggestion-card";

    const button = document.createElement("button");
    button.type = "button";
    button.className = "cover-suggestion-select";
    button.title = "Chọn ảnh bìa này";

    const image = document.createElement("img");
    image.src = suggestion.previewUrl || suggestion.imageUrl || context.fallbackUrl;
    image.alt = suggestion.title ? `Ảnh bìa ${suggestion.title}` : "Ảnh bìa từ Open Library";
    image.loading = "lazy";
    image.decoding = "async";
    image.referrerPolicy = "no-referrer";
    bindPreviewFallback(image, context.fallbackUrl);

    const copy = document.createElement("span");
    copy.className = "cover-suggestion-copy";

    const title = document.createElement("strong");
    title.textContent = suggestion.title || "Không rõ tên sách";

    const authors = document.createElement("small");
    authors.textContent = suggestion.authors || "Không rõ tác giả";

    copy.append(title, authors);
    button.append(image, copy);
    button.addEventListener("click", () => {
        context.urlInput.value = suggestion.imageUrl || suggestion.previewUrl || "";
        context.urlInput.dispatchEvent(new Event("change", { bubbles: true }));
        syncPreview(context.preview, suggestion.previewUrl || suggestion.imageUrl, context.fallbackUrl);
        setStatus(context.status, "Đã chọn ảnh bìa từ Open Library.", "success");
    });

    const sourceLink = document.createElement("a");
    sourceLink.className = "cover-suggestion-source";
    sourceLink.href = suggestion.openLibraryUrl || "https://openlibrary.org";
    sourceLink.target = "_blank";
    sourceLink.rel = "noopener noreferrer";
    sourceLink.textContent = "Open Library";

    card.append(button, sourceLink);
    return card;
}

function selectedAuthorText(select) {
    if (!select) {
        return "";
    }

    return Array.from(select.selectedOptions)
        .map((option) => option.textContent.trim())
        .filter(Boolean)
        .slice(0, 3)
        .join(" ");
}

function syncPreview(image, nextUrl, fallbackUrl) {
    const normalized = nextUrl?.trim();
    image.classList.remove("is-fallback-image");
    image.src = normalized || fallbackUrl;
}

function bindPreviewFallback(image, fallbackUrl) {
    image.dataset.imageFallback = fallbackUrl;
    image.addEventListener("error", () => {
        const absoluteFallback = new URL(fallbackUrl, window.location.href).href;
        if (image.src !== absoluteFallback) {
            image.src = absoluteFallback;
        }
        image.classList.add("is-fallback-image");
    });
}

function setStatus(status, message, tone) {
    if (!status) {
        return;
    }
    status.textContent = message;
    status.dataset.tone = tone;
}
