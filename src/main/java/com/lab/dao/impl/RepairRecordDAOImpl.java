package com.lab.dao.impl;

import com.lab.dao.RepairRecordDAO;
import com.lab.entity.RepairRecord;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 报修记录 DAO 实现。
 */
public class RepairRecordDAOImpl implements RepairRecordDAO {

    private static final Logger LOGGER = Logger.getLogger(RepairRecordDAOImpl.class.getName());

    private static final String BASE_SELECT =
            "SELECT rr.*,u.name AS user_name,e.eq_name AS equipment_name,e.img AS img_url "
                    + "FROM repair_record rr "
                    + "LEFT JOIN user u ON rr.user_id=u.id "
                    + "LEFT JOIN equipment e ON rr.equipment_id=e.id ";

    @Override
    public void add(RepairRecord record) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            add(conn, record);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public void add(Connection conn, RepairRecord record) {
        if (conn == null || record == null) {
            throw new DataAccessException("数据库连接不可用，无法新增报修记录", null);
        }
        String sql = "INSERT INTO repair_record(equipment_id,user_id,description,apply_time,status) "
                + "VALUES (?,?,?,?,?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, record.getEquipmentId());
            pstmt.setInt(2, record.getUserId());
            pstmt.setString(3, record.getDescription());
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(5, record.getStatus() == null ? 0 : record.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "新增报修记录失败", e);
            throw new DataAccessException("新增报修记录失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    @Override
    public void update(RepairRecord record) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            update(conn, record);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public boolean update(Connection conn, RepairRecord record) {
        if (conn == null || record == null || record.getId() == null) {
            return false;
        }
        String sql = "UPDATE repair_record SET status=?,complete_time=?,handler=?,remark=? WHERE id=?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, record.getStatus() == null ? 0 : record.getStatus());
            if (record.getCompleteTime() == null) {
                pstmt.setNull(2, Types.TIMESTAMP);
            } else {
                pstmt.setTimestamp(2, new Timestamp(record.getCompleteTime().getTime()));
            }
            setNullableString(pstmt, 3, record.getHandler());
            setNullableString(pstmt, 4, record.getRemark());
            pstmt.setInt(5, record.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "更新报修记录失败", e);
            throw new DataAccessException("更新报修记录失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    @Override
    public List<RepairRecord> findByUserId(Integer userId) {
        return queryList(null, BASE_SELECT + "WHERE rr.user_id=? ORDER BY rr.apply_time DESC", userId);
    }

    @Override
    public List<RepairRecord> findAll() {
        return queryList(null, BASE_SELECT + "ORDER BY rr.apply_time DESC");
    }

    @Override
    public RepairRecord findById(Integer id) {
        return queryOne(null, BASE_SELECT + "WHERE rr.id=?", id);
    }

    @Override
    public RepairRecord findById(Connection conn, Integer id) {
        return queryOne(conn, BASE_SELECT + "WHERE rr.id=?", id);
    }

    /**
     * 查询单条报修记录。
     *
     * @param conn   连接，null 时自建连接
     * @param sql    SQL
     * @param params 参数
     * @return 报修记录或 null
     */
    private RepairRecord queryOne(Connection conn, String sql, Object... params) {
        List<RepairRecord> list = queryList(conn, sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询报修记录列表。
     *
     * @param conn   连接，null 时自建连接
     * @param sql    SQL
     * @param params 参数
     * @return 报修记录列表
     */
    private List<RepairRecord> queryList(Connection conn, String sql, Object... params) {
        List<RepairRecord> list = new ArrayList<>();
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
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(rowToRepairRecord(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询报修记录失败", e);
            throw new DataAccessException("查询报修记录失败", e);
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
     * 可空字符串赋值。
     *
     * @param pstmt PreparedStatement
     * @param index 占位符序号
     * @param value 值
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
     * ResultSet 行映射为 RepairRecord 实体。
     *
     * @param rs 结果集
     * @return 报修记录
     * @throws SQLException 读取失败
     */
    private RepairRecord rowToRepairRecord(ResultSet rs) throws SQLException {
        RepairRecord record = new RepairRecord();
        record.setId(rs.getInt("id"));
        record.setEquipmentId(rs.getInt("equipment_id"));
        record.setUserId(rs.getInt("user_id"));
        record.setDescription(rs.getString("description"));
        record.setApplyTime(rs.getTimestamp("apply_time"));
        record.setCompleteTime(rs.getDate("complete_time"));
        record.setStatus(rs.getInt("status"));
        record.setHandler(rs.getString("handler"));
        record.setRemark(rs.getString("remark"));
        record.setUserName(rs.getString("user_name"));
        record.setEquipmentName(rs.getString("equipment_name"));
        record.setImgUrl(rs.getString("img_url"));
        return record;
    }
}
