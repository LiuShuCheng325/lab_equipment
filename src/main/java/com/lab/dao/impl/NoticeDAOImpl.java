package com.lab.dao.impl;

import com.lab.dao.NoticeDAO;
import com.lab.entity.Notice;
import com.lab.exception.DataAccessException;
import com.lab.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 站内提醒 DAO 实现。
 */
public class NoticeDAOImpl implements NoticeDAO {

    @Override
    public boolean add(Notice notice) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return add(conn, notice);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public boolean add(Connection conn, Notice notice) {
        if (conn == null || notice == null) {
            throw new DataAccessException("数据库连接不可用，无法写入提醒", null);
        }
        String sql = "INSERT INTO notice(user_id,borrow_id,type,title,content,is_read) "
                + "VALUES (?,?,?,?,?,0)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, notice.getUserId());
            if (notice.getBorrowId() == null) {
                pstmt.setNull(2, Types.INTEGER);
            } else {
                pstmt.setInt(2, notice.getBorrowId());
            }
            pstmt.setInt(3, notice.getType() == null ? Notice.TYPE_SYSTEM : notice.getType());
            pstmt.setString(4, notice.getTitle());
            if (notice.getContent() == null || notice.getContent().trim().isEmpty()) {
                pstmt.setNull(5, Types.VARCHAR);
            } else {
                pstmt.setString(5, notice.getContent());
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("新增提醒失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    @Override
    public List<Notice> findByUser(int userId, int offset, int limit) {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT * FROM notice WHERE user_id = ? "
                + "ORDER BY is_read ASC, id DESC LIMIT ?, ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return list;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, Math.max(offset, 0));
            pstmt.setInt(3, Math.max(limit, 1));
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(rowToNotice(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询提醒失败", e);
        } finally {
            DBUtil.close(null, pstmt, rs);
            DBUtil.closeQuietly(conn);
        }
        return list;
    }

    @Override
    public int countByUser(int userId) {
        return queryCount(null, "SELECT COUNT(*) FROM notice WHERE user_id = ?", userId);
    }

    @Override
    public int countUnread(int userId) {
        return queryCount(null,
                "SELECT COUNT(*) FROM notice WHERE user_id = ? AND is_read = 0", userId);
    }

    @Override
    public boolean markRead(int id, int userId) {
        return executeUpdate(null,
                "UPDATE notice SET is_read = 1 WHERE id = ? AND user_id = ?", id, userId) > 0;
    }

    @Override
    public boolean markAllRead(int userId) {
        return executeUpdate(null,
                "UPDATE notice SET is_read = 1 WHERE user_id = ? AND is_read = 0", userId) >= 0;
    }

    @Override
    public boolean existsRecent(int borrowId, int type, Date from) {
        return queryCount(null,
                "SELECT COUNT(*) FROM notice WHERE borrow_id = ? AND type = ? AND create_time >= ?",
                borrowId, type, from) > 0;
    }

    @Override
    public int deleteBefore(Date date) {
        return executeUpdate(null, "DELETE FROM notice WHERE create_time < ?", date);
    }

    /**
     * 通用增删改（支持事务连接）。
     *
     * @param conn   连接，null 时自建连接
     * @param sql    SQL
     * @param params 参数
     * @return 影响行数
     */
    private int executeUpdate(Connection conn, String sql, Object... params) {
        boolean ownConnection = (conn == null);
        Connection realConn = conn;
        PreparedStatement pstmt = null;
        try {
            if (ownConnection) {
                realConn = DBUtil.getConnection();
            }
            if (realConn == null) {
                return 0;
            }
            pstmt = realConn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("提醒数据操作失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
            if (ownConnection) {
                DBUtil.closeQuietly(realConn);
            }
        }
    }

    /**
     * 通用数量统计。
     *
     * @param conn   连接，null 时自建连接
     * @param sql    SQL
     * @param params 参数
     * @return 条数
     */
    private int queryCount(Connection conn, String sql, Object... params) {
        boolean ownConnection = (conn == null);
        Connection realConn = conn;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (ownConnection) {
                realConn = DBUtil.getConnection();
            }
            if (realConn == null) {
                return 0;
            }
            pstmt = realConn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("提醒数据统计失败", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                    // 忽略关闭异常
                }
            }
            DBUtil.closeQuietly(pstmt);
            if (ownConnection) {
                DBUtil.closeQuietly(realConn);
            }
        }
        return 0;
    }

    /**
     * ResultSet 行映射为 Notice 实体。
     *
     * @param rs 结果集
     * @return 提醒
     * @throws SQLException 读取失败
     */
    private Notice rowToNotice(ResultSet rs) throws SQLException {
        Notice notice = new Notice();
        notice.setId(rs.getInt("id"));
        notice.setUserId(rs.getInt("user_id"));
        Object borrowId = rs.getObject("borrow_id");
        notice.setBorrowId(borrowId == null ? null : ((Number) borrowId).intValue());
        notice.setType(rs.getInt("type"));
        notice.setTitle(rs.getString("title"));
        notice.setContent(rs.getString("content"));
        notice.setIsRead(rs.getInt("is_read"));
        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            notice.setCreateTime(new Date(createTime.getTime()));
        }
        return notice;
    }
}
