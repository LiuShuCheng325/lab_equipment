package com.lab.service;

import com.lab.entity.User;
import com.lab.util.Pagination;
import java.util.List;

public interface UserService {
    User login(String username, String password);
    boolean register(User user);
    List<User> findAll();

    /**
     * 按关键字分页查询账号（新增）。
     *
     * @param keyword  关键字，为空表示不筛选
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    Pagination<User> findPage(String keyword, int pageNum, int pageSize);
    User findById(Integer id);
    void update(User user);
    void delete(Integer id);
    boolean isUsernameExists(String username);
}