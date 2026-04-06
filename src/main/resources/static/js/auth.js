document.addEventListener("DOMContentLoaded", () => {
    bindPasswordToggles();
    showAuthFlash();
});

function bindPasswordToggles() {
    document.querySelectorAll("[data-toggle-password]").forEach((button) => {
        button.addEventListener("click", () => {
            const selector = button.dataset.togglePassword;
            if (!selector) {
                return;
            }

            const input = document.querySelector(selector);
            if (!input) {
                return;
            }

            const showing = input.type === "text";
            input.type = showing ? "password" : "text";
            button.setAttribute("aria-label", showing ? "Hiện hoặc ẩn mật khẩu" : "Ẩn mật khẩu");

            const icon = button.querySelector("i");
            if (icon) {
                icon.className = showing ? "fa-regular fa-eye" : "fa-regular fa-eye-slash";
            }
        });
    });
}

function showAuthFlash() {
    if (typeof window.Swal === "undefined") {
        return;
    }

    const body = document.body;
    const successMessage = (body.dataset.flashMsg || body.dataset.flashMessage || "").trim();
    const errorMessage = (body.dataset.flashError || "").trim();

    if (successMessage) {
        window.Swal.fire({
            icon: "success",
            title: "Thành công",
            text: successMessage,
            confirmButtonColor: "#0f766e"
        });
    }

    if (errorMessage) {
        window.Swal.fire({
            icon: "error",
            title: "Lỗi",
            text: errorMessage,
            confirmButtonColor: "#e11d48"
        });
    }
}
