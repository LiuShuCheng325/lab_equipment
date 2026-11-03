package com.lab.dao.impl;

import com.lab.dao.BorrowRecordDAO;
import com.lab.entity.BorrowRecord;
import com.lab.entity.BorrowStat;
import com.lab.entity.DayStat;
import com.lab.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 借用记录 DAO 实现。
 *
 * <p>所有 SQL 均使用 PreparedStatement 参数化，模糊查询使用 {@code LIKE CONCAT('%',?,'%')}。
 */
public class BorrowRecordDAOImpl implements BorrowRecordDAO {

    private static final Logger LOGGER = Logger.getLogger(BorrowRecordDAOImpl.class.getName());

    /** 一天的毫秒数，用于逾期天数计算。 */
    private static final long ONE_DAY_MILLIS = 24L * 60 * 60 * 1000;

    /** 基础查询：关联用户名与设备名。 */
    private static final String BASE_SELECT =
            "SELECT br.*, u.name AS userName, u.phone AS userPhone, e.eq_name AS equipmentName "
                    + "FROM borrow_record br "
                    + "LEFT JOIN user u ON br.user_id = u.id "
                    + "LEFT JOIN equipment e ON br.equipment_id = e.id ";

    // ==================== 新增 ====================

    @Override
    public void add(BorrowRecord record) {
        addReturnId(record);
    }

    @Override
    public int addReturnId(BorrowRecord record) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return insert(conn, record);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public int add(Connection conn, BorrowRecord record) {
        return insert(conn, record);
    }

    /**
     * 执行 INSERT 并返回自增主键。
     *
     * @param conn   数据库连接（不可为 null）
     * @param record 借用记录
     * @return 新记录主键，0 表示失败
     */
    private int insert(Connection conn, BorrowRecord record) {
        if (conn == null || record == null) {
            throw new com.lab.exception.DataAccessException("数据库连接不可用，无法新增借用记录", null);
        }
        String sql = "INSERT INTO borrow_record"
                + "(user_id,equipment_id,quantity,status,borrow_time,expect_return_time,remark) "
                + "VALUES (?,?,?,?,?,?,?)";
        PreparedStatement pstmt = null;
        ResultSet keys = null;
        try {
            pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getEquipmentId());
            pstmt.setInt(3, record.getQuantity());
            pstmt.setInt(4, record.getStatus() == null ? BorrowRecord.STATUS_PENDING : record.getStatus());
            pstmt.setTimestamp(5, toTimestamp(record.getBorrowTime()));
            pstmt.setTimestamp(6, toTimestamp(record.getExpectReturnTime()));
            setNullableString(pstmt, 7, record.getRemark());
            int rows = pstmt.executeUpdate();
            if (rows <= 0) {
                return 0;
            }
            keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                record.setId(id);
                return id;
            }
            return 0;
        } catch (SQLException e) {
            throw new com.lab.exception.DataAccessException("新增借用记录失败", e);
        } finally {
            if (keys != null) {
                try {
                    keys.close();
                } catch (SQLException ignored) {
                    LOGGER.log(Level.FINEST, "关闭自增主键结果集失败", ignored);
                }
            }
            DBUtil.closeQuietly(pstmt);
        }
    }

    // ==================== 更新 ====================

    @Override
    public void update(BorrowRecord record) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            update(conn, record);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public boolean update(Connection conn, BorrowRecord record) {
        if (conn == null || record == null || record.getId() == null) {
            return false;
        }
        String sql = "UPDATE borrow_record SET user_id=?,equipment_id=?,quantity=?,status=?,"
                + "borrow_time=?,expect_return_time=?,return_time=?,approver=?,remark=?,"
                + "audit_remark=?,return_condition=?,remind_count=?,last_remind_time=? WHERE id=?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, record.getUserId());
            pstmt.setInt(2, record.getEquipmentId());
            pstmt.setInt(3, record.getQuantity());
            pstmt.setInt(4, record.getStatus() == null ? BorrowRecord.STATUS_PENDING : record.getStatus());
            pstmt.setTimestamp(5, toTimestamp(record.getBorrowTime()));
            pstmt.setTimestamp(6, toTimestamp(record.getExpectReturnTime()));
            pstmt.setTimestamp(7, toTimestamp(record.getActualReturnTime()));
            setNullableString(pstmt, 8, record.getRemark());
            setNullableString(pstmt, 9, record.getAuditRemark());
            setNullableInt(pstmt, 10, record.getReturnCondition());
            pstmt.setInt(11, record.getRemindCount() == null ? 0 : record.getRemindCount());
            pstmt.setTimestamp(12, toTimestamp(record.getLastRemindTime()));
            pstmt.setInt(13, record.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new com.lab.exception.DataAccessException("更新借用记录失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    @Override
    public boolean updateApprove(Connection conn, Integer id, Integer status,
                                 String approver, String auditRemark) {
        if (conn == null || id == null || status == null) {
            return false;
        }
        String sql = "UPDATE borrow_record SET status=?, approver=?, audit_remark=? "
                + "WHERE id=? AND status=0";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            setNullableString(pstmt, 2, approver);
            setNullableString(pstmt, 3, auditRemark);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new com.lab.exception.DataAccessException("审批借用记录失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    @Override
    public boolean updateReturn(Connection conn, Integer id, Integer status,
                                Date returnTime, Integer returnCondition) {
        if (conn == null || id == null || status == null) {
            return false;
        }
        String sql = "UPDATE borrow_record SET status=?, return_time=?, return_condition=? "
                + "WHERE id=? AND status IN (1,4)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setTimestamp(2, toTimestamp(returnTime));
            setNullableInt(pstmt, 3, returnCondition);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new com.lab.exception.DataAccessException("归还借用记录失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    @Override
    public boolean updateStatus(Integer id, Integer status) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return updateStatus(conn, id, status);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public boolean updateStatus(Connection conn, Integer id, Integer status) {
        if (conn == null || id == null || status == null) {
            return false;
        }
        String sql = "UPDATE borrow_record SET status=? WHERE id=?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new com.lab.exception.DataAccessException("更新借用状态失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    @Override
    public boolean updateRemind(Connection conn, Integer id, int remindCount, Date lastRemindTime) {
        if (conn == null || id == null) {
            return false;
        }
        String sql = "UPDATE borrow_record SET remind_count=?, last_remind_time=? WHERE id=?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, remindCount);
            pstmt.setTimestamp(2, toTimestamp(lastRemindTime));
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new com.lab.exception.DataAccessException("更新提醒状态失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    // ==================== 查询 ====================

    @Override
    public BorrowRecord findById(Integer id) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return findById(conn, id);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public BorrowRecord findById(Connection conn, Integer id) {
        return queryOne(conn, BASE_SELECT + "WHERE br.id = ?", id);
    }

    @Override
    public BorrowRecord findByIdForUpdate(Connection conn, Integer id) {
        return queryOne(conn, BASE_SELECT + "WHERE br.id = ? FOR UPDATE", id);
    }

    @Override
    public List<BorrowRecord> findByUserId(Integer userId) {
        return queryList(null, BASE_SELECT + "WHERE br.user_id = ? ORDER BY br.id DESC", userId);
    }

    @Override
    public List<BorrowRecord> findAll() {
        return queryList(null, BASE_SELECT + "ORDER BY br.id DESC");
    }

    @Override
    public List<BorrowRecord> findPending() {
        return queryList(null, BASE_SELECT + "WHERE br.status = 0 ORDER BY br.id DESC");
    }

    @Override
    public List<BorrowRecord> findOverdue() {
        return queryList(null, BASE_SELECT
                + "WHERE br.status IN (1,4) AND br.expect_return_time IS NOT NULL "
                + "AND br.expect_return_time < NOW() ORDER BY br.expect_return_time ASC");
    }

    @Override
    public List<BorrowRecord> findOverdueByUser(int userId) {
        return queryList(null, BASE_SELECT
                + "WHERE br.user_id = ? AND br.status IN (1,4) AND br.expect_return_time IS NOT NULL "
                + "AND br.expect_return_time < NOW() ORDER BY br.expect_return_time ASC", userId);
    }

    @Override
    public List<BorrowRecord> findDueSoon(int days) {
        return queryList(null, BASE_SELECT
                + "WHERE br.status = 1 AND br.expect_return_time IS NOT NULL "
                + "AND br.expect_return_time >= NOW() "
                + "AND br.expect_return_time <= DATE_ADD(NOW(), INTERVAL ? DAY) "
                + "ORDER BY br.expect_return_time ASC", days);
    }

    @Override
    public List<BorrowRecord> findByStatusAndDueBefore(int status, Date time) {
        return queryList(null, BASE_SELECT
                + "WHERE br.status = ? AND br.expect_return_time IS NOT NULL "
                + "AND br.expect_return_time < ? ORDER BY br.expect_return_time ASC", status, time);
    }

    @Override
    public boolean existsActive(int userId, int equipmentId) {
        String sql = "SELECT COUNT(*) FROM borrow_record "
                + "WHERE user_id=? AND equipment_id=? AND status IN (0,1,4)";
        return queryCount(null, sql, userId, equipmentId) > 0;
    }

    @Override
    public List<BorrowRecord> findPage(Integer userId, Integer status, String keyword,
                                       int offset, int limit) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, status, keyword, params);
        String sql = BASE_SELECT + where + " ORDER BY br.id DESC LIMIT ?, ?";
        params.add(Math.max(offset, 0));
        params.add(Math.max(limit, 1));
        return queryList(null, sql, params.toArray());
    }

    @Override
    public int count(Integer userId, Integer status, String keyword) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, status, keyword, params);
        String sql = "SELECT COUNT(*) FROM borrow_record br "
                + "LEFT JOIN user u ON br.user_id = u.id "
                + "LEFT JOIN equipment e ON br.equipment_id = e.id " + where;
        return queryCount(null, sql, params.toArray());
    }

    @Override
    public BorrowStat statistics() {
        BorrowStat stat = new BorrowStat();
        String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN status=0 THEN 1 ELSE 0 END) AS pending_count, "
                + "SUM(CASE WHEN status=1 THEN 1 ELSE 0 END) AS borrowing_count, "
                + "SUM(CASE WHEN status=2 THEN 1 ELSE 0 END) AS rejected_count, "
                + "SUM(CASE WHEN status=3 THEN 1 ELSE 0 END) AS returned_count, "
                + "SUM(CASE WHEN status=4 THEN 1 ELSE 0 END) AS overdue_count, "
                + "SUM(CASE WHEN status=1 AND expect_return_time IS NOT NULL "
                + "  AND expect_return_time >= NOW() "
                + "  AND expect_return_time <= DATE_ADD(NOW(), INTERVAL 3 DAY) "
                + "  THEN 1 ELSE 0 END) AS due_soon_count "
                + "FROM borrow_record";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return stat;
            }
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stat.setTotal(rs.getInt("total"));
                stat.setPending(rs.getInt("pending_count"));
                stat.setBorrowing(rs.getInt("borrowing_count"));
                stat.setRejected(rs.getInt("rejected_count"));
                stat.setReturned(rs.getInt("returned_count"));
                stat.setOverdue(rs.getInt("overdue_count"));
                stat.setDueSoon(rs.getInt("due_soon_count"));
            }
        } catch (SQLException e) {
            throw new com.lab.exception.DataAccessException("统计借用记录失败", e);
        } finally {
            DBUtil.close(null, pstmt, rs);
            DBUtil.closeQuietly(conn);
        }
        return stat;
    }

    @Override
    public int countActiveByEquipment(Integer equipmentId) {
        String sql = "SELECT COUNT(*) FROM borrow_record "
                + "WHERE equipment_id = ? AND status IN (0,1,4)";
        return queryCount(null, sql, equipmentId);
    }

    @Override
    public Map<Integer, Integer> countBorrowingByEquipment() {
        Map<Integer, Integer> result = new HashMap<>();
        String sql = "SELECT equipment_id, SUM(quantity) AS total FROM borrow_record "
                + "WHERE status IN (1,4) GROUP BY equipment_id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return result;
            }
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getInt("equipment_id"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            throw new com.lab.exception.DataAccessException("统计设备借出数量失败", e);
        } finally {
            DBUtil.close(null, pstmt, rs);
            DBUtil.closeQuietly(conn);
        }
        return result;
    }

    @Override
    public List<DayStat> borrowTrend(int days) {
        int totalDays = days <= 0 ? 7 : days;
        Map<String, Integer> rawMap = new HashMap<>();
        String sql = "SELECT DATE(borrow_time) AS stat_date, COUNT(*) AS total FROM borrow_record "
                + "WHERE borrow_time >= DATE_SUB(CURDATE(), INTERVAL ? DAY) "
                + "GROUP BY DATE(borrow_time)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return buildEmptyTrend(totalDays);
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, totalDays - 1);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                rawMap.put(rs.getString("stat_date"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            throw new com.lab.exception.DataAccessException("查询借用趋势失败", e);
        } finally {
            DBUtil.close(null, pstmt, rs);
            DBUtil.closeQuietly(conn);
        }

        List<DayStat> trend = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -(totalDays - 1));
        for (int i = 0; i < totalDays; i++) {
            String day = sdf.format(calendar.getTime());
            Integer count = rawMap.get(day);
            trend.add(new DayStat(day, count == null ? 0 : count));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return trend;
    }

    /**
     * 构造没有数据时的趋势列表。
     *
     * @param totalDays 天数
     * @return 全 0 的趋势列表
     */
    private List<DayStat> buildEmptyTrend(int totalDays) {
        List<DayStat> trend = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -(totalDays - 1));
        for (int i = 0; i < totalDays; i++) {
            trend.add(new DayStat(sdf.format(calendar.getTime()), 0));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return trend;
    }

    /**
     * 拼接动态 WHERE 条件（全部参数化）。
     *
     * @param userId  用户ID，null 表示不限
     * @param status  状态，null 表示不限
     * @param keyword 关键字，空串表示不限
     * @param params  参数容器（出参）
     * @return WHERE 子句（无条件是空串）
     */
    private String buildWhere(Integer userId, Integer status, String keyword, List<Object> params) {
        StringBuilder sb = new StringBuilder();
        if (userId != null) {
            sb.append(" AND br.user_id = ?");
            params.add(userId);
        }
        if (status != null) {
            sb.append(" AND br.status = ?");
            params.add(status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append(" AND (e.eq_name LIKE CONCAT('%',?,'%') "
                    + "OR u.name LIKE CONCAT('%',?,'%') "
                    + "OR br.remark LIKE CONCAT('%',?,'%'))");
            String kw = keyword.trim();
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        return sb.length() == 0 ? "" : " WHERE 1=1" + sb.toString();
    }

    /**
     * 查询单条记录。
     *
     * @param conn  连接，null 时自建连接
     * @param sql   SQL
     * @param params 参数
     * @return 借用记录或 null
     */
    private BorrowRecord queryOne(Connection conn, String sql, Object... params) {
        List<BorrowRecord> list = queryList(conn, sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询列表。
     *
     * @param conn  连接，null 时自建连接
     * @param sql   SQL
     * @param params 参数
     * @return 借用记录列表
     */
    private List<BorrowRecord> queryList(Connection conn, String sql, Object... params) {
        List<BorrowRecord> list = new ArrayList<>();
        boolean ownConnection = (conn == null);
        Connection realConn = conn;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (ownConnection) {
                realConn = DBUtil.getConnection();
            }
            if (realConn == null) {
                return list;
            }
            pstmt = realConn.prepareStatement(sql);
            fillParams(pstmt, params);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(rowToRecord(rs));
            }
        } catch (SQLException e) {
            throw new com.lab.exception.DataAccessException("查询借用记录失败", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                    LOGGER.log(Level.FINEST, "关闭 ResultSet 失败", ignored);
                }
            }
            DBUtil.closeQuietly(pstmt);
            if (ownConnection) {
                DBUtil.closeQuietly(realConn);
            }
        }
        return list;
    }

    /**
     * 查询数量。
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
            fillParams(pstmt, params);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new com.lab.exception.DataAccessException("统计借用记录数量失败", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                    LOGGER.log(Level.FINEST, "关闭 ResultSet 失败", ignored);
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
     * 填充 PreparedStatement 占位符。
     *
     * @param pstmt  PreparedStatement
     * @param params 参数
     * @throws SQLException 赋值失败
     */
    private void fillParams(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    /**
     * 可空字符串赋值。
     *
     * @param pstmt  PreparedStatement
     * @param index  占位符序号（从 1 开始）
     * @param value  值，null 时写入 SQL NULL
     * @throws SQLException 赋值失败
     */
    private void setNullableString(PreparedStatement pstmt, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            pstmt.setNull(index, Types.VARCHAR);
        } else {
            pstmt.setString(index, value);
        }
    }

    /**
     * 可空整数赋值。
     *
     * @param pstmt  PreparedStatement
     * @param index  占位符序号（从 1 开始）
     * @param value  值，null 时写入 SQL NULL
     * @throws SQLException 赋值失败
     */
    private void setNullableInt(PreparedStatement pstmt, int index, Integer value) throws SQLException {
        if (value == null) {
            pstmt.setNull(index, Types.INTEGER);
        } else {
            pstmt.setInt(index, value);
        }
    }

    /**
     * Date 转 Timestamp。
     *
     * @param date 日期，可为 null
     * @return Timestamp 或 null
     */
    private Timestamp toTimestamp(Date date) {
        return date == null ? null : new Timestamp(date.getTime());
    }

    /**
     * ResultSet 行映射为 BorrowRecord 实体。
     *
     * <p>逾期判定与逾期扫描（OverdueService）保持完全一致：
     * 已通过(status=1)或已逾期(status=4) 且 预计归还时间早于"实际归还时间或当前时间"即算逾期。
     *
     * @param rs 结果集
     * @return 借用记录
     * @throws SQLException 读取失败
     */
    private BorrowRecord rowToRecord(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(rs.getInt("id"));
        record.setUserId(rs.getInt("user_id"));
        record.setEquipmentId(rs.getInt("equipment_id"));
        record.setQuantity(rs.getInt("quantity"));
        record.setStatus(rs.getInt("status"));
        record.setRemark(rs.getString("remark"));
        record.setApprover(rs.getString("approver"));
        record.setAuditRemark(rs.getString("audit_remark"));

        Object returnCondition = rs.getObject("return_condition");
        record.setReturnCondition(returnCondition == null ? null : ((Number) returnCondition).intValue());
        Object remindCount = rs.getObject("remind_count");
        record.setRemindCount(remindCount == null ? 0 : ((Number) remindCount).intValue());

        Timestamp borrowTs = rs.getTimestamp("borrow_time");
        Timestamp expectTs = rs.getTimestamp("expect_return_time");
        Timestamp returnTs = rs.getTimestamp("return_time");
        Timestamp remindTs = rs.getTimestamp("last_remind_time");

        if (borrowTs != null) {
            record.setBorrowTime(new Date(borrowTs.getTime()));
        }
        if (expectTs != null) {
            record.setExpectReturnTime(new Date(expectTs.getTime()));
        }
        if (returnTs != null) {
            record.setActualReturnTime(new Date(returnTs.getTime()));
        }
        if (remindTs != null) {
            record.setLastRemindTime(new Date(remindTs.getTime()));
        }

        record.setOverdueDays(calcOverdueDays(record.getStatus(), expectTs, returnTs));

        // 关联查询得到的用户名、设备名
        record.setUserName(rs.getString("userName"));
        record.setUserPhone(rs.getString("userPhone"));
        record.setEquipmentName(rs.getString("equipmentName"));
        return record;
    }

    /**
     * 计算逾期天数（向上取整，不足一天按一天计）。
     *
     * @param status   记录状态
     * @param expectTs 预计归还时间
     * @param returnTs 实际归还时间（可为 null）
     * @return 逾期天数，0 表示未逾期
     */
    static int calcOverdueDays(Integer status, Timestamp expectTs, Timestamp returnTs) {
        if (expectTs == null || status == null) {
            return 0;
        }
        long endMillis;
        if (returnTs != null) {
            // 已归还：以实际归还时间为准判断是否逾期归还
            endMillis = returnTs.getTime();
        } else if (status == BorrowRecord.STATUS_APPROVED || status == BorrowRecord.STATUS_OVERDUE) {
            // 未归还且处于借用中/已逾期：以当前时间为准
            endMillis = System.currentTimeMillis();
        } else {
            return 0;
        }
        long diff = endMillis - expectTs.getTime();
        if (diff <= 0) {
            return 0;
        }
        long days = diff / ONE_DAY_MILLIS;
        if (diff % ONE_DAY_MILLIS > 0) {
            days++;
        }
        return (int) days;
    }
}
