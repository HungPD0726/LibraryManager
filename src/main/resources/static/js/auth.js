document.addEventListener("DOMContentLoaded", () => {
    bindLoginExperience();
    bindPasswordToggles();
    showAuthFlash();
});

function bindLoginExperience() {
    const form = document.querySelector("[data-auth-login-form]");
    if (!form) {
        return;
    }

    bindLoginFieldStates(form);
    bindCapsLockWarning(form);
    bindLoginSubmitState(form);
}

function bindPasswordToggles() {
    document.querySelectorAll("[data-toggle-password]").forEach((button) => {
        const selector = button.dataset.togglePassword;
        if (!selector) {
            return;
        }

        const input = document.querySelector(selector);
        if (!input) {
            return;
        }

        syncPasswordToggleState(button, input);
        button.addEventListener("click", () => {
            input.type = input.type === "text" ? "password" : "text";
            syncPasswordToggleState(button, input);
        });
    });
}

function syncPasswordToggleState(button, input) {
    const showing = input.type === "text";
    button.setAttribute("aria-pressed", String(showing));
    button.setAttribute("aria-label", showing ? "Ẩn mật khẩu" : "Hiện mật khẩu");

    const icon = button.querySelector("i");
    if (icon) {
        icon.className = showing ? "fa-regular fa-eye-slash" : "fa-regular fa-eye";
    }
}

function bindLoginFieldStates(form) {
    const inputs = form.querySelectorAll("[data-auth-input]");
    if (!inputs.length) {
        return;
    }

    const syncState = (input) => {
        const field = input.closest("[data-auth-field]");
        if (!field) {
            return;
        }

        field.classList.toggle("is-filled", input.value.length > 0);
        field.classList.toggle("is-focused", document.activeElement === input);
    };

    inputs.forEach((input) => {
        ["input", "change", "focus", "blur"].forEach((eventName) => {
            input.addEventListener(eventName, () => {
                syncState(input);
            });
        });

        syncState(input);
    });

    window.setTimeout(() => {
        inputs.forEach((input) => {
            syncState(input);
        });
    }, 150);
}

function bindCapsLockWarning(form) {
    const passwordInput = form.querySelector("[data-auth-password-input]");
    const warning = form.querySelector("[data-login-caps-warning]");
    const field = passwordInput?.closest("[data-auth-field]");
    if (!passwordInput || !warning || !field) {
        return;
    }

    const setCapsLockState = (capsLockOn) => {
        warning.hidden = !capsLockOn;
        warning.classList.toggle("is-visible", capsLockOn);
        field.classList.toggle("has-caps-warning", capsLockOn);
    };

    const syncFromKeyboardEvent = (event) => {
        if (typeof event.getModifierState !== "function") {
            return;
        }

        setCapsLockState(event.getModifierState("CapsLock"));
    };

    ["keydown", "keyup"].forEach((eventName) => {
        passwordInput.addEventListener(eventName, syncFromKeyboardEvent);
    });

    passwordInput.addEventListener("blur", () => {
        setCapsLockState(false);
    });
}

function bindLoginSubmitState(form) {
    const submitButton = form.querySelector("[data-auth-submit-button]");
    const submitLabel = form.querySelector("[data-auth-submit-label]");
    const submitIcon = form.querySelector("[data-auth-submit-icon]");
    if (!submitButton || !submitLabel || !submitIcon) {
        return;
    }

    const defaultLabel = submitLabel.textContent.trim() || "Đăng nhập";
    const pendingLabel = "Đang đăng nhập...";

    const resetSubmitState = () => {
        delete form.dataset.submitting;
        submitButton.disabled = false;
        submitButton.classList.remove("is-pending");
        submitButton.removeAttribute("aria-busy");
        submitButton.removeAttribute("aria-disabled");
        submitLabel.textContent = defaultLabel;
        submitIcon.className = "fa-solid fa-arrow-right-to-bracket";
    };

    resetSubmitState();

    form.addEventListener("submit", (event) => {
        if (form.dataset.submitting === "true") {
            event.preventDefault();
            return;
        }

        if (typeof form.checkValidity === "function" && !form.checkValidity()) {
            return;
        }

        form.dataset.submitting = "true";
        submitButton.disabled = true;
        submitButton.classList.add("is-pending");
        submitButton.setAttribute("aria-busy", "true");
        submitButton.setAttribute("aria-disabled", "true");
        submitLabel.textContent = pendingLabel;
        submitIcon.className = "fa-solid fa-spinner";
    });

    window.addEventListener("pageshow", resetSubmitState);
}

function showAuthFlash() {
    if (typeof window.Swal === "undefined") {
        return;
    }

    const loginPage = document.querySelector("[data-auth-login-page]");
    if (loginPage?.querySelector("[data-login-inline-alert]")) {
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
