package com.lab.dao.impl;

import com.lab.exception.DataAccessException;
import com.lab.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO 基类，封装通用数据库增删改操作，消除所有 DAO 实现类重复代码。
 *
 * <p>SQL 执行失败时抛出 {@link DataAccessException}（RuntimeException），
 * 不再静默 printStackTrace，保证上层可以感知失败。
 */
@SuppressWarnings({"SqlDialectInspection", "DuplicatedCode"})
public abstract class BaseDAO {

    private static final Logger LOGGER = Logger.getLogger(BaseDAO.class.getName());

    /**
     * 通用增删改执行方法（自动提交，自建连接）。
     *
     * @param sql    SQL 语句
     * @param params 可变参数列表，自动适配类型
     * @return 影响行数
     */
    protected int executeUpdate(String sql, Object... params) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return executeUpdate(conn, sql, params);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * 通用增删改执行方法（可传入外部连接参与事务）。
     *
     * @param conn   数据库连接
     * @param sql    SQL 语句
     * @param params 可变参数列表
     * @return 影响行数
     */
    protected int executeUpdate(Connection conn, String sql, Object... params) {
        if (conn == null) {
            throw new DataAccessException("数据库连接不可用，无法执行更新操作", null);
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = conn.prepareStatement(sql);
            // 循环给占位符赋值
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param == null) {
                    // MySQL 字符串/文本空字段统一使用 VARCHAR，兼容性更好
                    preparedStatement.setNull(i + 1, Types.VARCHAR);
                } else {
                    preparedStatement.setObject(i + 1, param);
                }
            }
            // 执行增删改
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "执行数据库更新操作失败: " + sql, e);
            throw new DataAccessException("数据库操作失败", e);
        } finally {
            // 统一关闭 Statement（连接由调用方管理）
            DBUtil.closeQuietly(preparedStatement);
        }
    }
}
