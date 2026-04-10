const prefersReducedMotion = window.matchMedia?.("(prefers-reduced-motion: reduce)")?.matches ?? false;

document.addEventListener("DOMContentLoaded", () => {
    bindSidebarToggle();
    bindStudentNavToggle();
    bindAlerts();
    bindProfileMenu();
    bindPendingBorrowCount();
    bindRippleEffect();
    bindScrollProgress();
    bindNumberCounters();
});

function bindSidebarToggle() {
    const body = document.body;
    const sidebar = document.getElementById("sidebar");
    const toggles = document.querySelectorAll("[data-sidebar-toggle]");
    const backdrop = document.querySelector("[data-sidebar-backdrop]");
    if (!body.classList.contains("admin-shell") || !sidebar || !toggles.length) {
        return;
    }

    const desktopBreakpoint = 1080;
    const storageKey = body.dataset.sidebarStorageKey || "libraryManager.adminSidebarCollapsed";
    const navLinks = sidebar.querySelectorAll(".shell-nav a");

    const isDesktop = () => window.innerWidth > desktopBreakpoint;

    const syncBodyScrollLock = () => {
        body.classList.toggle("is-scroll-locked", !isDesktop() && body.classList.contains("sidebar-open"));
    };

    const readCollapsedState = () => {
        try {
            return localStorage.getItem(storageKey) === "true";
        } catch (error) {
            return body.classList.contains("sidebar-collapsed");
        }
    };

    const persistCollapsedState = (collapsed) => {
        try {
            localStorage.setItem(storageKey, String(collapsed));
        } catch (error) {
            console.debug("Không thể lưu trạng thái menu quản trị.", error);
        }
    };

    const syncToggleState = () => {
        const expanded = isDesktop()
            ? !body.classList.contains("sidebar-collapsed")
            : body.classList.contains("sidebar-open");

        toggles.forEach((toggle) => {
            toggle.setAttribute("aria-expanded", String(expanded));
        });
    };

    const closeMobileSidebar = () => {
        body.classList.remove("sidebar-open");
        syncToggleState();
        syncBodyScrollLock();
    };

    const applyDesktopSidebarState = (collapsed) => {
        body.classList.toggle("sidebar-collapsed", collapsed);
        syncToggleState();
        syncBodyScrollLock();
    };

    const syncResponsiveSidebarState = () => {
        if (isDesktop()) {
            closeMobileSidebar();
            applyDesktopSidebarState(readCollapsedState());
            return;
        }

        body.classList.remove("sidebar-collapsed");
        syncToggleState();
        syncBodyScrollLock();
    };

    toggles.forEach((toggle) => {
        toggle.addEventListener("click", () => {
            if (isDesktop()) {
                const nextCollapsed = !body.classList.contains("sidebar-collapsed");
                persistCollapsedState(nextCollapsed);
                applyDesktopSidebarState(nextCollapsed);
                return;
            }

            body.classList.toggle("sidebar-open");
            syncToggleState();
            syncBodyScrollLock();
        });
    });

    backdrop?.addEventListener("click", closeMobileSidebar);

    navLinks.forEach((link) => {
        link.addEventListener("click", () => {
            if (!isDesktop()) {
                closeMobileSidebar();
            }
        });
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && body.classList.contains("sidebar-open")) {
            closeMobileSidebar();
        }
    });

    window.addEventListener("resize", syncResponsiveSidebarState);
    syncResponsiveSidebarState();
}

function bindStudentNavToggle() {
    const toggles = document.querySelectorAll("[data-student-nav-toggle]");
    if (!toggles.length) {
        return;
    }

    toggles.forEach((toggle) => {
        const topbar = toggle.closest(".student-topbar");
        const nav = topbar ? topbar.querySelector(".student-nav") : null;
        if (!topbar || !nav) {
            return;
        }

        const closeMenu = () => {
            topbar.classList.remove("is-nav-open");
            toggle.setAttribute("aria-expanded", "false");
        };

        toggle.addEventListener("click", () => {
            const open = topbar.classList.toggle("is-nav-open");
            toggle.setAttribute("aria-expanded", String(open));
        });

        nav.querySelectorAll("a").forEach((link) => {
            link.addEventListener("click", closeMenu);
        });

        document.addEventListener("click", (event) => {
            if (window.innerWidth > 840) {
                closeMenu();
                return;
            }

            if (!topbar.contains(event.target)) {
                closeMenu();
            }
        });

        document.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                closeMenu();
            }
        });

        window.addEventListener("resize", () => {
            if (window.innerWidth > 840) {
                closeMenu();
            }
        });
    });
}

function bindAlerts() {
    document.querySelectorAll("[data-dismiss-alert]").forEach((button) => {
        button.addEventListener("click", () => {
            const alert = button.closest(".alert");
            if (alert) {
                alert.remove();
            }
        });
    });

    document.querySelectorAll(".alert").forEach((alert) => {
        window.setTimeout(() => {
            if (alert.isConnected) {
                alert.remove();
            }
        }, 6000);
    });
}

function bindProfileMenu() {
    const menus = document.querySelectorAll("[data-profile-menu]");
    if (!menus.length) {
        return;
    }

    menus.forEach((menu) => {
        menu.addEventListener("toggle", () => {
            if (!menu.open) {
                return;
            }

            menus.forEach((otherMenu) => {
                if (otherMenu !== menu) {
                    otherMenu.removeAttribute("open");
                }
            });
        });
    });

    document.addEventListener("click", (event) => {
        menus.forEach((menu) => {
            if (menu.open && !menu.contains(event.target)) {
                menu.removeAttribute("open");
            }
        });
    });

    document.addEventListener("keydown", (event) => {
        if (event.key !== "Escape") {
            return;
        }

        menus.forEach((menu) => {
            menu.removeAttribute("open");
        });
    });
}

function bindPendingBorrowCount() {
    const countNode = document.querySelector("[data-pending-count]");
    if (!countNode || !document.body.classList.contains("admin-shell")) {
        return;
    }

    const endpoint = countNode.dataset.pendingCountUrl;
    if (!endpoint) {
        return;
    }

    fetch(endpoint, {
        headers: {
            Accept: "application/json"
        },
        cache: "no-store",
        credentials: "same-origin"
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error(`Unexpected status: ${response.status}`);
            }
            return response.json();
        })
        .then((payload) => {
            const value = Number(payload?.pendingCount);
            if (Number.isFinite(value)) {
                countNode.textContent = String(value);
            }
        })
        .catch((error) => {
            console.debug("Không thể tải số yêu cầu mượn đang chờ.", error);
        });
}

function bindRippleEffect() {
    if (prefersReducedMotion) {
        return;
    }

    document.querySelectorAll(".btn").forEach((button) => {
        button.addEventListener("click", function handleButtonClick(event) {
            if (this.disabled) {
                return;
            }

            const ripple = document.createElement("span");
            ripple.classList.add("btn-ripple");
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            ripple.style.width = `${size}px`;
            ripple.style.height = `${size}px`;
            ripple.style.left = `${event.clientX - rect.left - size / 2}px`;
            ripple.style.top = `${event.clientY - rect.top - size / 2}px`;
            this.appendChild(ripple);
            window.setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
}

function bindScrollProgress() {
    const progress = document.getElementById("scrollProgress");
    if (!progress) {
        return;
    }

    const updateScrollProgress = () => {
        const winScroll = document.documentElement.scrollTop || document.body.scrollTop;
        const height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
        const scrolled = height > 0 ? (winScroll / height) * 100 : 0;
        progress.style.width = `${scrolled}%`;
    };

    window.addEventListener("scroll", updateScrollProgress, { passive: true });
    updateScrollProgress();
}

function bindNumberCounters() {
    if (prefersReducedMotion) {
        return;
    }

    const counters = document.querySelectorAll(".metric-card strong, .hero-mini-card strong, .revenue-summary-card strong");
    counters.forEach((counter) => {
        const rawText = counter.innerText.trim();
        if (!rawText || rawText.includes("VND")) {
            return;
        }

        const isCurrency = rawText.includes("₫");
        const normalized = rawText.replace(/[^\d]/g, "");
        if (!normalized) {
            return;
        }

        const target = Number.parseInt(normalized, 10);
        if (!Number.isFinite(target) || target <= 0) {
            return;
        }

        const step = Math.max(1, Math.ceil(target / 36));
        let current = 0;

        const renderValue = (value) => {
            const formatted = new Intl.NumberFormat("vi-VN").format(value);
            counter.innerText = isCurrency ? `${formatted} ₫` : formatted;
        };

        const updateCount = () => {
            current += step;
            if (current < target) {
                renderValue(current);
                window.requestAnimationFrame(updateCount);
                return;
            }

            counter.innerText = rawText;
        };

        window.requestAnimationFrame(updateCount);
    });
}
