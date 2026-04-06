document.addEventListener("DOMContentLoaded", () => {
    bindSidebarToggle();
    bindStudentNavToggle();
    bindAlerts();
    bindProfileMenu();
});

function bindSidebarToggle() {
    const toggles = document.querySelectorAll("[data-sidebar-toggle]");
    if (!toggles.length) {
        return;
    }

    toggles.forEach((toggle) => {
        toggle.addEventListener("click", () => {
            document.body.classList.toggle("sidebar-open");
        });
    });
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
