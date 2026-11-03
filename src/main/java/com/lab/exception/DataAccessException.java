package com.lab.exception;

/**
 * 数据访问异常：DAO 层 SQL 执行失败时抛出（统一替代 printStackTrace）。
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message 异常说明
     * @param cause   原始 SQLException
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
