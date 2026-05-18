/* 全站统一的 flash 提示弹窗处理。
 * 约定：
 * - 目标节点 class: js-flash-alert
 * - 可选属性 data-alert-mode:
 *   - always: 总是弹
 *   - top-only: 仅顶层窗口弹
 *   - top-or-user-shell: 顶层或父窗口为 /user/home 时弹
 *   - top-or-shell: 顶层或父窗口为 /user/home、/admin/home 时弹
 */
(function () {
    var lastAlertMessage = '';
    var lastAlertAt = 0;
    var DEDUP_WINDOW_MS = 1200;

    function canAlert(mode) {
        if (!mode || mode === 'always') {
            return true;
        }
        if (mode === 'top-only') {
            return window.top === window.self;
        }
        if (mode === 'top-or-user-shell') {
            if (window.top === window.self) {
                return true;
            }
            try {
                return window.top.location && window.top.location.pathname === '/user/home';
            } catch (e) {
                return false;
            }
        }
        if (mode === 'top-or-shell') {
            if (window.top === window.self) {
                return true;
            }
            try {
                var p = window.top.location && window.top.location.pathname;
                return p === '/user/home' || p === '/admin/home';
            } catch (e) {
                return false;
            }
        }
        return true;
    }

    function show(message, mode) {
        var text = (message || '').trim();
        if (!text) {
            return false;
        }
        if (!canAlert(mode || 'always')) {
            return false;
        }
        var now = Date.now();
        if (text === lastAlertMessage && (now - lastAlertAt) <= DEDUP_WINDOW_MS) {
            return false;
        }
        lastAlertMessage = text;
        lastAlertAt = now;
        window.alert(text);
        return true;
    }

    function flush(selector) {
        var alerts = document.querySelectorAll(selector || '.js-flash-alert');
        if (!alerts || alerts.length === 0) {
            return;
        }
        Array.prototype.forEach.call(alerts, function (el) {
            var message = (el.textContent || '').trim();
            var mode = el.getAttribute('data-alert-mode') || 'always';
            show(message, mode);
            // 不影响原页面布局
            if (el.parentNode) {
                el.parentNode.removeChild(el);
            }
        });
    }

    window.NcpFlashAlert = {
        canAlert: canAlert,
        show: show,
        flush: flush
    };

    flush('.js-flash-alert');
})();
