package com.lab.util;

import com.lab.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 数据库连接工具类。
 *
 * <p>配置优先级：
 * <ol>
 *   <li>classpath 下的 {@code db.properties}（键：jdbc.driver / jdbc.url / jdbc.username / jdbc.password）</li>
 *   <li>系统属性 {@code lab.db.password} 等（用于临时覆盖）</li>
 *   <li>代码内硬编码兜底值（仅用于配置文件缺失场景）</li>
 * </ol>
 *
 * <p>同时提供轻量事务支持：Service 层通过 {@link #beginTransaction()} 拿到
 * autoCommit=false 的连接，DAO 层提供接收 {@link Connection} 的重载方法参与同一事务。
 */
public final class DBUtil {

    private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());

    private static final String CONFIG_FILE = "db.properties";

    /** 硬编码兜底：驱动。 */
    private static final String FALLBACK_DRIVER = "com.mysql.cj.jdbc.Driver";

    /** 硬编码兜底：URL。 */
    private static final String FALLBACK_URL =
            "jdbc:mysql://localhost:3306/lab_equipment?useUnicode=true&characterEncoding=UTF-8"
                    + "&serverTimezone=Asia/Shanghai&useSSL=false";

    /** 硬编码兜底：用户名。 */
    private static final String FALLBACK_USER = "root";

    /** 硬编码兜底：密码（正常情况以 db.properties 为准）。 */
    private static final String FALLBACK_PASSWORD = "root";

    private static final Properties CONFIG = new Properties();

    static {
        loadConfig();
        try {
            Class.forName(resolveDriver());
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL 驱动加载失败，请检查依赖中是否包含 mysql-connector-j", e);
        }
    }

    private DBUtil() {
    }

    /**
     * 加载 classpath 下的 db.properties。
     */
    private static void loadConfig() {
        try (InputStream in = DBUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                LOGGER.warning("未找到 classpath:" + CONFIG_FILE + "，将使用硬编码兜底配置");
                return;
            }
            CONFIG.load(in);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "读取 " + CONFIG_FILE + " 失败，将使用硬编码兜底配置", e);
        }
    }

    /**
     * 读取配置：优先 db.properties，其次系统属性，最后兜底常量。
     *
     * @param propKey    db.properties 中的键
     * @param sysKey     系统属性键
     * @param fallback   兜底值
     * @return 解析后的配置值
     */
    private static String resolve(String propKey, String sysKey, String fallback) {
        String value = CONFIG.getProperty(propKey);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        String sysValue = System.getProperty(sysKey);
        if (sysValue != null && !sysValue.trim().isEmpty()) {
            return sysValue.trim();
        }
        return fallback;
    }

    private static String resolveDriver() {
        return resolve("jdbc.driver", "lab.db.driver", FALLBACK_DRIVER);
    }

    private static String resolveUrl() {
        return resolve("jdbc.url", "lab.db.url", FALLBACK_URL);
    }

    private static String resolveUser() {
        return resolve("jdbc.username", "lab.db.username", FALLBACK_USER);
    }

    private static String resolvePassword() {
        return resolve("jdbc.password", "lab.db.password", FALLBACK_PASSWORD);
    }

    /**
     * 获取一条数据库连接（自动提交）。
     *
     * <p>失败时返回 null（保持与历史代码兼容），异常写入日志，不再向控制台打印噪音日志。
     *
     * @return 数据库连接，失败返回 null
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(resolveUrl(), resolveUser(), resolvePassword());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "获取数据库连接失败，请检查 db.properties 配置与 MySQL 服务状态", e);
            return null;
        }
    }

    /**
     * 开启事务：返回 autoCommit=false 的连接。
     *
     * @return 事务连接
     * @throws SQLException 获取连接或设置事务属性失败
     */
    public static Connection beginTransaction() throws SQLException {
        Connection conn = DriverManager.getConnection(resolveUrl(), resolveUser(), resolvePassword());
        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        } catch (SQLException e) {
            closeQuietly(conn);
            throw e;
        }
        return conn;
    }

    /**
     * 提交事务。
     *
     * @param conn 事务连接
     */
    public static void commit(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new DataAccessException("事务提交失败", e);
        }
    }

    /**
     * 回滚事务（不抛异常，保证 finally 中可安全调用）。
     *
     * @param conn 事务连接
     */
    public static void rollback(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "事务回滚失败", e);
        }
    }

    /**
     * 静默关闭连接（不清空 autoCommit 标记，调用方自行保证不再复用）。
     *
     * @param conn 连接
     */
    public static void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "关闭数据库连接失败", e);
        }
    }

    /**
     * 关闭 Statement 与 Connection（事务连接请改用 {@link #closeQuietly(Connection)}）。
     *
     * @param conn   连接
     * @param stmt   Statement / PreparedStatement
     */
    public static void close(Connection conn, Statement stmt) {
        closeQuietly(stmt);
        closeQuietly(conn);
    }

    /**
     * 关闭 ResultSet、Statement 与 Connection。
     *
     * @param conn   连接
     * @param stmt   Statement / PreparedStatement
     * @param rs     结果集
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "关闭 ResultSet 失败", e);
            }
        }
        close(conn, stmt);
    }

    /**
     * 静默关闭 Statement。
     *
     * @param stmt Statement / PreparedStatement
     */
    public static void closeQuietly(Statement stmt) {
        if (stmt == null) {
            return;
        }
        try {
            stmt.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "关闭 Statement 失败", e);
        }
    }

    /**
     * 手动测试数据库连通性。
     *
     * @param args 运行参数（未使用）
     */
    public static void main(String[] args) {
        Connection conn = getConnection();
        LOGGER.info("数据库连接测试结果：" + (conn != null ? "成功" : "失败"));
        closeQuietly(conn);
    }
}
