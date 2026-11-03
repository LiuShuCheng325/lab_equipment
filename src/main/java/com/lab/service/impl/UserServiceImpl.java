package com.lab.service.impl;

import com.lab.dao.UserDAO;
import com.lab.dao.impl.UserDAOImpl;
import com.lab.entity.User;
import com.lab.service.UserService;
import com.lab.util.Pagination;
import com.lab.util.SHA256Util;

import java.util.List;
import java.util.Objects;

public class UserServiceImpl implements UserService {

    private final UserDAO userDAO = new UserDAOImpl();

    /**
     * 登录校验：用户名查询 + SHA256 密文比对。
     *
     * <p>安全说明：不再打印任何与密码相关的调试日志。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 登录成功返回用户对象，失败返回 null
     */
    @Override
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            return null;
        }
        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            return null;
        }
        return Objects.equals(SHA256Util.encrypt(password), user.getPassword()) ? user : null;
    }


    @Override
    public boolean register(User user) {
        if (isUsernameExists(user.getUsername())) {
            return false;
        }
        // 密码加密
        user.setPassword(SHA256Util.encrypt(user.getPassword()));
        // 默认普通用户
        user.setRole(0);
        userDAO.add(user);
        return true;
    }

    @Override
    public List<User> findAll() {
        return userDAO.findAll();
    }

    /**
     * 分页查询账号：先统计总数，再取当前页数据，封装为 Pagination。
     */
    @Override
    public Pagination<User> findPage(String keyword, int pageNum, int pageSize) {
        Pagination<User> page = new Pagination<>(pageNum, pageSize);
        page.setTotal(userDAO.count(keyword));
        page.setData(userDAO.findPage(keyword, page.getStartIndex(), page.getPageSize()));
        return page;
    }

    @Override
    public User findById(Integer id) {
        return userDAO.findById(id);
    }

    @Override
    public void update(User user) {
        userDAO.update(user);
    }

    @Override
    public void delete(Integer id) {
        userDAO.delete(id);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userDAO.findByUsername(username) != null;
    }
}