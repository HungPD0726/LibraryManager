document.addEventListener("DOMContentLoaded", () => {
    bindBorrowLineManager();
});

function bindBorrowLineManager() {
    const linesContainer = document.querySelector("[data-borrow-lines]");
    const addButton = document.querySelector("[data-add-borrow-line]");
    const template = document.getElementById("borrowLineTemplate");

    if (!linesContainer || !addButton || !template) {
        return;
    }

    addButton.addEventListener("click", () => {
        linesContainer.insertAdjacentHTML("beforeend", template.innerHTML.trim());
    });

    linesContainer.addEventListener("click", (event) => {
        const button = event.target.closest("[data-remove-borrow-line]");
        if (!button) {
            return;
        }

        const line = button.closest(".borrow-line");
        if (!line) {
            return;
        }

        const allLines = linesContainer.querySelectorAll(".borrow-line");
        if (allLines.length <= 1) {
            line.querySelectorAll("select, input").forEach((field) => {
                if (field.tagName === "SELECT") {
                    field.selectedIndex = 0;
                } else if (field.type === "number") {
                    field.value = 1;
                } else {
                    field.value = "";
                }
            });
            return;
        }

        line.remove();
    });
}
