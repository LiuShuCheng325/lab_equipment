package com.lab.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Web 层通用工具：参数安全读取 + 基于 session 的一次性提示（flash message）。
 *
 * <p>提示传递约定（后端统一）：所有写操作使用 <b>POST + 重定向</b>，
 * 提示文案写入 session 的 flash 区，下一次 GET 请求读取后立即清除；
 * 读取时会同时写入 request 属性 {@code msg} / {@code errMsg}，
 * 因此现有 JSP 中的 {@code ${msg}} / {@code ${errMsg}} 无需改动即可生效。
 */
public final class WebUtil {

    /** session 中存放成功提示的 key。 */
    private static final String FLASH_MSG = "flashMsg";

    /** session 中存放错误提示的 key。 */
    private static final String FLASH_ERR = "flashErr";

    private WebUtil() {
    }

    /**
     * 读取请求参数并做 trim，空值返回默认字符串。
     *
     * @param request      请求对象
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 去空格后的参数值
     */
    public static String str(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        if (value == null) {
            return defaultValue;
        }
        value = value.trim();
        return value.isEmpty() ? defaultValue : value;
    }

    /**
     * 读取整数参数，非法值返回默认值。
     *
     * @param request      请求对象
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 整数参数值
     */
    public static int intParam(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 读取可空的整数参数（空串/null 返回 null）。
     *
     * @param request 请求对象
     * @param name    参数名
     * @return 整数值或 null
     */
    public static Integer intOrNull(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 限制字符串最大长度（用于用户输入入库前的统一收敛）。
     *
     * @param value   原始值
     * @param maxLen  最大长度
     * @return 截断后的值，null 安全
     */
    public static String limit(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLen) {
            return trimmed;
        }
        return trimmed.substring(0, maxLen);
    }

    /**
     * 写入成功提示（一次性）。
     *
     * @param session 会话
     * @param msg     提示文案
     */
    public static void flashMsg(HttpSession session, String msg) {
        if (session != null && msg != null && !msg.isBlank()) {
            session.setAttribute(FLASH_MSG, msg);
        }
    }

    /**
     * 写入错误提示（一次性）。
     *
     * @param session 会话
     * @param err     提示文案
     */
    public static void flashErr(HttpSession session, String err) {
        if (session != null && err != null && !err.isBlank()) {
            session.setAttribute(FLASH_ERR, err);
    }
    }

    /**
     * 取出并清除 session 中的一次性提示，写入 request 属性供 JSP 展示。
     *
     * @param request 请求对象
     */
    public static void consumeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object msg = session.getAttribute(FLASH_MSG);
        if (msg != null) {
            request.setAttribute("msg", msg);
            session.removeAttribute(FLASH_MSG);
        }
        Object err = session.getAttribute(FLASH_ERR);
        if (err != null) {
            request.setAttribute("errMsg", err);
            session.removeAttribute(FLASH_ERR);
        }
    }
}
