/* ================================================================
   全站公共脚本（原生 JS，无任何外部依赖）
   提供：一次性提示（flash）→ Toast、通用 Toast、提交按钮加载态、
        通用弹窗 open/close（用于「驳回」等需要填写意见的场景）
   ================================================================ */
(function () {
    'use strict';

    var TOAST_DURATION = 2500;

    /**
     * 弹出一条 Toast 提示。
     * @param {string} message 提示文案
     * @param {string} type    ok | warn | danger，缺省为普通提示
     */
    function showToast(message, type) {
        var host = document.getElementById('toastHost');
        if (!host || !message) {
            return;
        }
        var el = document.createElement('div');
        el.className = 'toast' + (type ? ' toast--' + type : '');
        el.textContent = message;
        host.appendChild(el);

        window.requestAnimationFrame(function () {
            el.classList.add('is-show');
        });

        window.setTimeout(function () {
            el.classList.remove('is-show');
            window.setTimeout(function () {
                if (el.parentNode) {
                    el.parentNode.removeChild(el);
                }
            }, 200);
        }, TOAST_DURATION);
    }

    /** 打开指定弹窗。 */
    function openModal(id) {
        var mask = document.getElementById(id);
        if (mask) {
            mask.classList.add('is-open');
        }
    }

    /** 关闭指定弹窗。 */
    function closeModal(id) {
        var mask = document.getElementById(id);
        if (mask) {
            mask.classList.remove('is-open');
        }
    }

    window.App = {
        toast: showToast,
        openModal: openModal,
        closeModal: closeModal
    };

    document.addEventListener('click', function (event) {
        var target = event.target;
        if (!target || !target.getAttribute) {
            return;
        }
        var toOpen = target.getAttribute('data-modal-open');
        if (toOpen) {
            openModal(toOpen);
        }
        var toClose = target.getAttribute('data-modal-close');
        if (toClose) {
            closeModal(toClose);
        }
        if (target.classList && target.classList.contains('modal-mask')) {
            target.classList.remove('is-open');
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            var masks = document.querySelectorAll('.modal-mask.is-open');
            for (var i = 0; i < masks.length; i++) {
                masks[i].classList.remove('is-open');
            }
        }
    });

    /* 表单提交时给按钮加 loading 态，避免重复提交 */
    document.addEventListener('submit', function (event) {
        var form = event.target;
        if (!form || form.tagName !== 'FORM') {
            return;
        }
        var btn = form.querySelector('button[type="submit"]');
        if (!btn || btn.disabled || btn.dataset.noLoading === '1') {
            return;
        }
        btn.classList.add('is-loading');
        btn.disabled = true;
        window.setTimeout(function () {
            btn.classList.remove('is-loading');
            btn.disabled = false;
        }, 5000);
    });

    document.addEventListener('DOMContentLoaded', function () {
        var host = document.getElementById('toastHost');
        if (!host) {
            return;
        }
        var okMsg = host.getAttribute('data-msg');
        var errMsg = host.getAttribute('data-err');
        if (okMsg) {
            showToast(okMsg, 'ok');
        }
        if (errMsg) {
            showToast(errMsg, 'danger');
        }
    });
})();
