package com.lab.dao;

import com.lab.entity.User;
import java.util.List;

public interface UserDAO {
    User findByUsername(String username);
    User findById(Integer id);
    List<User> findAll();

    /**
     * 按关键字分页查询账号（新增，供用户管理列表使用）。
     *
     * @param keyword   关键字，为空表示不筛选
     * @param startIndex SQL LIMIT 偏移量
     * @param pageSize  每页条数
     * @return 当前页账号列表
     */
    List<User> findPage(String keyword, int startIndex, int pageSize);

    /**
     * 统计符合关键字的账号数量（新增）。
     *
     * @param keyword 关键字，为空表示不筛选
     * @return 账号总数
     */
    int count(String keyword);
    void add(User user);
    void update(User user);
    void delete(Integer id);
}