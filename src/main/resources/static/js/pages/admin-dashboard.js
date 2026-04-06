document.addEventListener("DOMContentLoaded", () => {
    if (typeof window.Chart === "undefined") {
        return;
    }

    const monthlyData = readJsonPayload("dashboardMonthlyBorrowData", {});
    const categoryData = readJsonPayload("dashboardCategoryData", {});
    const revenueData = readJsonPayload("dashboardRevenueData", {});

    window.Chart.defaults.font.family = "'Manrope', sans-serif";
    window.Chart.defaults.color = "#64748b";

    renderMonthlyBorrowChart(monthlyData);
    renderCategoryChart(categoryData);
    renderRevenueChart(revenueData);
});

function readJsonPayload(elementId, fallbackValue) {
    const element = document.getElementById(elementId);
    if (!element) {
        return fallbackValue;
    }

    const raw = element.textContent ? element.textContent.trim() : "";
    if (!raw) {
        return fallbackValue;
    }

    try {
        return JSON.parse(raw);
    } catch (error) {
        console.warn(`Cannot parse dashboard payload: ${elementId}`, error);
        return fallbackValue;
    }
}

function renderMonthlyBorrowChart(monthlyData) {
    const chart = document.getElementById("monthlyBorrowChart");
    if (!chart) {
        return;
    }

    const monthLabels = Object.keys(monthlyData).map((month) => `Tháng ${month}`);
    const monthValues = Object.values(monthlyData);
    if (!monthValues.length) {
        return;
    }

    new window.Chart(chart, {
        type: "bar",
        data: {
            labels: monthLabels,
            datasets: [{
                label: "Lượt mượn",
                data: monthValues,
                backgroundColor: "#2e9e8f",
                borderRadius: 4,
                barPercentage: 0.6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0 },
                    grid: { color: "#f1f5f9" }
                },
                x: {
                    grid: { display: false }
                }
            }
        }
    });
}

function renderCategoryChart(categoryData) {
    const chart = document.getElementById("categoryChart");
    if (!chart) {
        return;
    }

    const labels = Object.keys(categoryData);
    const values = Object.values(categoryData);
    if (!values.length) {
        return;
    }

    new window.Chart(chart, {
        type: "doughnut",
        data: {
            labels,
            datasets: [{
                data: values,
                backgroundColor: [
                    "#2e9e8f", "#fbbf24", "#f87171", "#38bdf8",
                    "#818cf8", "#a78bfa", "#34d399"
                ],
                borderWidth: 0,
                hoverOffset: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: "right",
                    labels: {
                        boxWidth: 12,
                        usePointStyle: true
                    }
                }
            },
            cutout: "70%"
        }
    });
}

function renderRevenueChart(revenueData) {
    const chart = document.getElementById("revenueChart");
    if (!chart) {
        return;
    }

    const labels = Array.isArray(revenueData.labels) ? revenueData.labels : [];
    const orderRevenue = Array.isArray(revenueData.orderRevenue) ? revenueData.orderRevenue : [];
    const fineRevenue = Array.isArray(revenueData.fineRevenue) ? revenueData.fineRevenue : [];
    const totalRevenue = Array.isArray(revenueData.totalRevenue) ? revenueData.totalRevenue : [];
    if (!labels.length) {
        return;
    }

    new window.Chart(chart, {
        data: {
            labels,
            datasets: [
                {
                    type: "bar",
                    label: "Đơn mua đã giao",
                    data: orderRevenue,
                    backgroundColor: "rgba(15, 118, 110, 0.78)",
                    borderRadius: 8,
                    maxBarThickness: 28
                },
                {
                    type: "bar",
                    label: "Phí/phạt đã thu",
                    data: fineRevenue,
                    backgroundColor: "rgba(245, 158, 11, 0.78)",
                    borderRadius: 8,
                    maxBarThickness: 28
                },
                {
                    type: "line",
                    label: "Tổng doanh thu",
                    data: totalRevenue,
                    borderColor: "#1d4ed8",
                    backgroundColor: "rgba(29, 78, 216, 0.12)",
                    tension: 0.35,
                    borderWidth: 3,
                    fill: false,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    pointBackgroundColor: "#1d4ed8"
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: "index",
                intersect: false
            },
            plugins: {
                legend: {
                    position: "top",
                    align: "end"
                },
                tooltip: {
                    callbacks: {
                        label: (context) => `${context.dataset.label}: ${Number(context.raw || 0).toLocaleString("vi-VN")} ₫`
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: (value) => formatCompactCurrency(value)
                    },
                    grid: { color: "#f1f5f9" }
                },
                x: {
                    stacked: false,
                    grid: { display: false }
                }
            }
        }
    });
}

function formatCompactCurrency(value) {
    const numeric = Number(value || 0);
    if (Math.abs(numeric) >= 1000000000) {
        return `${(numeric / 1000000000).toFixed(1).replace(/\.0$/, "")}B`;
    }
    if (Math.abs(numeric) >= 1000000) {
        return `${(numeric / 1000000).toFixed(1).replace(/\.0$/, "")}M`;
    }
    if (Math.abs(numeric) >= 1000) {
        return `${(numeric / 1000).toFixed(0)}K`;
    }
    return numeric.toLocaleString("vi-VN");
}
