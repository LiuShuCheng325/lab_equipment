package com.lab.dao.impl;

import com.lab.dao.EquipmentDAO;
import com.lab.entity.Equipment;
import com.lab.exception.DataAccessException;
import com.lab.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备 DAO 实现。
 *
 * <p>库存变更一律走参数化 UPDATE，扣减带 {@code available >= ?} 下界保护，
 * 归还用 {@code LEAST(available + ?, stock)} 上界保护。
 */
public class EquipmentDAOImpl implements EquipmentDAO {

    private static final String BASE_SELECT = "SELECT * FROM equipment ";

    @Override
    public List<Equipment> findAll() {
        return queryList(null, BASE_SELECT + "ORDER BY id DESC");
    }

    @Override
    public List<Equipment> findPage(String keyword, String eqType, Integer status,
                                    int offset, int limit) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(keyword, eqType, status, params);
        String sql = BASE_SELECT + where + " ORDER BY id DESC LIMIT ?, ?";
        params.add(Math.max(offset, 0));
        params.add(Math.max(limit, 1));
        return queryList(null, sql, params.toArray());
    }

    @Override
    public int count(String keyword, String eqType, Integer status) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(keyword, eqType, status, params);
        return queryCount(null, "SELECT COUNT(*) FROM equipment " + where, params.toArray());
    }

    @Override
    public Equipment findById(Integer id) {
        return queryOne(null, BASE_SELECT + "WHERE id = ?", id);
    }

    @Override
    public Equipment findById(Connection conn, Integer id) {
        return queryOne(conn, BASE_SELECT + "WHERE id = ?", id);
    }

    @Override
    public boolean add(Equipment equipment) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return add(conn, equipment);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public boolean add(Connection conn, Equipment equipment) {
        String sql = "INSERT INTO equipment"
                + "(eq_name,model,eq_type,stock,available,location,remark,status,img,create_time) "
                + "VALUES (?,?,?,?,?,?,?,?,?,NOW())";
        PreparedStatement pstmt = null;
        try {
            requireConnection(conn);
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, equipment.getEqName());
            setNullableString(pstmt, 2, equipment.getModel());
            setNullableString(pstmt, 3, equipment.getEqType());
            pstmt.setInt(4, equipment.getStock());
            pstmt.setInt(5, equipment.getAvailable() == null ? equipment.getStock() : equipment.getAvailable());
            setNullableString(pstmt, 6, equipment.getLocation());
            setNullableString(pstmt, 7, equipment.getRemark());
            pstmt.setInt(8, equipment.getStatus() == null ? Equipment.STATUS_NORMAL : equipment.getStatus());
            setNullableString(pstmt, 9, equipment.getImg());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("新增设备失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    @Override
    public boolean update(Equipment equipment) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return update(conn, equipment);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public boolean update(Connection conn, Equipment equipment) {
        String sql = "UPDATE equipment SET eq_name=?,model=?,eq_type=?,stock=?,available=?,"
                + "location=?,remark=?,status=?,img=? WHERE id=?";
        PreparedStatement pstmt = null;
        try {
            requireConnection(conn);
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, equipment.getEqName());
            setNullableString(pstmt, 2, equipment.getModel());
            setNullableString(pstmt, 3, equipment.getEqType());
            pstmt.setInt(4, equipment.getStock());
            pstmt.setInt(5, equipment.getAvailable() == null ? 0 : equipment.getAvailable());
            setNullableString(pstmt, 6, equipment.getLocation());
            setNullableString(pstmt, 7, equipment.getRemark());
            pstmt.setInt(8, equipment.getStatus() == null ? Equipment.STATUS_NORMAL : equipment.getStatus());
            setNullableString(pstmt, 9, equipment.getImg());
            pstmt.setInt(10, equipment.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("更新设备失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM equipment WHERE id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            requireConnection(conn);
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("删除设备失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public boolean updateAvailable(Integer id, Integer quantity) {
        if (id == null || quantity == null || quantity == 0) {
            return false;
        }
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            requireConnection(conn);
            if (quantity > 0) {
                return restoreAvailable(conn, id, quantity) > 0;
            }
            return deductAvailable(conn, id, -quantity) > 0;
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public int deductAvailable(Connection conn, Integer id, int quantity) {
        // 关键：available >= ? 保证并发审批不会把库存扣成负数
        String sql = "UPDATE equipment SET available = available - ? WHERE id = ? AND available >= ?";
        PreparedStatement pstmt = null;
        try {
            requireConnection(conn);
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, id);
            pstmt.setInt(3, quantity);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("扣减设备库存失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    @Override
    public int restoreAvailable(Connection conn, Integer id, int quantity) {
        // 关键：LEAST 保证归还后 available 不超过 stock
        String sql = "UPDATE equipment SET available = LEAST(available + ?, stock) WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            requireConnection(conn);
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("恢复设备库存失败", e);
        } finally {
            DBUtil.closeQuietly(pstmt);
        }
    }

    @Override
    public List<String> findCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT eq_type FROM equipment "
                + "WHERE eq_type IS NOT NULL AND eq_type <> '' ORDER BY eq_type";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return categories;
            }
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                categories.add(rs.getString("eq_type"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询设备类别失败", e);
        } finally {
            DBUtil.close(null, pstmt, rs);
            DBUtil.closeQuietly(conn);
        }
        return categories;
    }

    @Override
    public Map<Integer, Integer> countBorrowingByEquipment() {
        Map<Integer, Integer> result = new LinkedHashMap<>();
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
            throw new DataAccessException("统计设备借出数量失败", e);
        } finally {
            DBUtil.close(null, pstmt, rs);
            DBUtil.closeQuietly(conn);
        }
        return result;
    }

    @Override
    public int countAvailable() {
        return queryCount(null,
                "SELECT COUNT(*) FROM equipment WHERE status = 0 AND available > 0");
    }

    @Override
    public Map<String, Integer> countByCategory() {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT IFNULL(eq_type,'未分类') AS category, COUNT(*) AS total "
                + "FROM equipment GROUP BY IFNULL(eq_type,'未分类') ORDER BY total DESC";
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
                result.put(rs.getString("category"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计设备类别分布失败", e);
        } finally {
            DBUtil.close(null, pstmt, rs);
            DBUtil.closeQuietly(conn);
        }
        return result;
    }

    /**
     * 拼接动态 WHERE 条件（全部参数化）。
     *
     * @param keyword 关键字
     * @param eqType  类别
     * @param status  状态
     * @param params  参数容器（出参）
     * @return WHERE 子句
     */
    private String buildWhere(String keyword, String eqType, Integer status, List<Object> params) {
        StringBuilder sb = new StringBuilder();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sb.append(" AND (eq_name LIKE CONCAT('%',?,'%') "
                    + "OR model LIKE CONCAT('%',?,'%') "
                    + "OR location LIKE CONCAT('%',?,'%') "
                    + "OR remark LIKE CONCAT('%',?,'%'))");
            String kw = keyword.trim();
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }
        if (eqType != null && !eqType.trim().isEmpty()) {
            sb.append(" AND eq_type = ?");
            params.add(eqType.trim());
        }
        if (status != null) {
            sb.append(" AND status = ?");
            params.add(status);
        }
        return sb.length() == 0 ? "" : " WHERE 1=1" + sb.toString();
    }

    /**
     * 校验连接可用。
     *
     * @param conn 连接
     */
    private void requireConnection(Connection conn) {
        if (conn == null) {
            throw new DataAccessException("数据库连接不可用", null);
        }
    }

    /**
     * 查询单条设备。
     *
     * @param conn   连接，null 时自建连接
     * @param sql    SQL
     * @param params 参数
     * @return 设备或 null
     */
    private Equipment queryOne(Connection conn, String sql, Object... params) {
        List<Equipment> list = queryList(conn, sql, params);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询设备列表。
     *
     * @param conn   连接，null 时自建连接
     * @param sql    SQL
     * @param params 参数
     * @return 设备列表
     */
    private List<Equipment> queryList(Connection conn, String sql, Object... params) {
        List<Equipment> list = new ArrayList<>();
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
                list.add(rowToEquipment(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("查询设备失败", e);
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
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("统计设备数量失败", e);
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
     * ResultSet 行映射为 Equipment 实体。
     *
     * @param rs 结果集
     * @return 设备
     * @throws SQLException 读取失败
     */
    private Equipment rowToEquipment(ResultSet rs) throws SQLException {
        Equipment equipment = new Equipment();
        equipment.setId(rs.getInt("id"));
        equipment.setEqName(rs.getString("eq_name"));
        equipment.setModel(rs.getString("model"));
        equipment.setEqType(rs.getString("eq_type"));
        equipment.setStock(rs.getInt("stock"));
        equipment.setAvailable(rs.getInt("available"));
        equipment.setLocation(rs.getString("location"));
        equipment.setRemark(rs.getString("remark"));
        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            equipment.setCreateTime(new Date(createTime.getTime()));
        }
        equipment.setStatus(rs.getInt("status"));
        equipment.setImg(rs.getString("img"));
        return equipment;
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
}
