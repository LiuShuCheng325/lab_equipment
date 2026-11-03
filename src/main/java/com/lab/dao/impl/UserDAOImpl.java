package com.lab.dao.impl;

import com.lab.dao.UserDAO;
import com.lab.entity.User;
import com.lab.util.DBUtil;
import com.lab.util.Pagination;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAOImpl extends BaseDAO implements UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());

    @Override
    public User findByUsername(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return null;
            }
            String sql = "SELECT * FROM user WHERE username=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowToUser(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询用户数据失败", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public User findById(Integer id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return null;
            }
            String sql = "SELECT * FROM user WHERE id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowToUser(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询用户数据失败", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return list;
            }
            String sql = "SELECT * FROM user ORDER BY id DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(rowToUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "查询用户数据失败", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    @Override
    public List<User> findPage(String keyword, int startIndex, int pageSize) {
        List<User> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return list;
            }
            String sql = "SELECT * FROM user " + keywordWhere(keyword)
                    + "ORDER BY id DESC LIMIT ?, ?";
            pstmt = conn.prepareStatement(sql);
            int idx = bindKeyword(pstmt, keyword, 1);
            pstmt.setInt(idx++, startIndex < 0 ? 0 : startIndex);
            pstmt.setInt(idx, pageSize < 1 ? Pagination.DEFAULT_PAGE_SIZE : pageSize);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(rowToUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "分页查询账号数据失败", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return list;
    }

    @Override
    public int count(String keyword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return 0;
            }
            String sql = "SELECT COUNT(*) FROM user " + keywordWhere(keyword);
            pstmt = conn.prepareStatement(sql);
            bindKeyword(pstmt, keyword, 1);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "统计账号数量失败", e);
        } finally {
            DBUtil.close(conn, pstmt, rs);
        }
        return 0;
    }

    /**
     * 构造关键字筛选的 WHERE 子句。
     */
    private String keywordWhere(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "";
        }
        return "WHERE username LIKE ? OR name LIKE ? OR phone LIKE ? OR email LIKE ? ";
    }

    /**
     * 绑定关键字模糊查询参数，返回下一个可用参数下标。
     */
    private int bindKeyword(PreparedStatement pstmt, String keyword, int startIndex)
            throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return startIndex;
        }
        String like = "%" + keyword.trim() + "%";
        int idx = startIndex;
        for (int i = 0; i < 4; i++) {
            pstmt.setString(idx++, like);
        }
        return idx;
    }

    @Override
    public void add(User user) {
        String sql = "INSERT INTO user(username,password,name,phone,email,role) VALUES (?,?,?,?,?,?)";
        executeUpdate(sql,
                user.getUsername(),
                user.getPassword(),
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole());
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE user SET name=?,phone=?,email=?,role=? WHERE id=?";
        executeUpdate(sql,
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getId());
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM user WHERE id=?";
        executeUpdate(sql, id);
    }

    private User rowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setPhone(rs.getString("phone"));
        user.setEmail(rs.getString("email"));
        // 兼容数据库role为NULL，无值默认设为0
        Object roleObj = rs.getObject("role");
        if(roleObj != null){
            user.setRole((Integer) roleObj);
        }else{
            user.setRole(0);
        }
        return user;
    }
}