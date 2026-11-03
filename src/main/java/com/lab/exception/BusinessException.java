package com.lab.exception;

/**
 * 业务异常：用于 Service 层向 Servlet 层传递"用户可读"的失败原因。
 *
 * <p>继承 RuntimeException，DAO/Service 方法签名无需声明 throws，
 * 由 Servlet 统一捕获后放入 session flash，页面展示提示文案。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 仅带提示信息的构造。
     *
     * @param message 面向用户的提示文案
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * 带提示信息与原始异常的构造。
     *
     * @param message 面向用户的提示文案
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
