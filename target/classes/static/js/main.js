/**
 * Smart Campus Event System - Main JavaScript
 */

document.addEventListener('DOMContentLoaded', function () {

    // Auto-dismiss flash alert messages after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity .4s ease, transform .4s ease';
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-8px)';
            setTimeout(function () { alert.remove(); }, 400);
        }, 5000);
    });

    // Set minimum date for date inputs to today (for new event form)
    const dateInputs = document.querySelectorAll('input[type="date"]');
    const today = new Date().toISOString().split('T')[0];
    dateInputs.forEach(function (input) {
        if (!input.value) {
            input.setAttribute('min', today);
        }
    });

    // Active link highlight for sidebar (based on current URL)
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-list a').forEach(function (link) {
        if (link.getAttribute('href') === currentPath) {
            link.classList.add('active');
        }
    });

    // Seat progress bar live update
    document.querySelectorAll('.seat-bar').forEach(function (bar) {
        const fill = bar.querySelector('.seat-fill');
        if (fill) {
            const w = parseInt(fill.style.width);
            if (w >= 90) fill.style.background = '#ef4444';
            else if (w >= 70) fill.style.background = '#f59e0b';
        }
    });

    // Confirm dialogs are inline (onclick="return confirm(...)"), no extra JS needed

    // Search input debounce (for live search UX)
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        let timeout;
        searchInput.addEventListener('input', function () {
            clearTimeout(timeout);
            timeout = setTimeout(function () {
                const form = document.getElementById('filter-form');
                if (form) form.submit();
            }, 700);
        });
    }
});
