package com.lab.service;

import com.lab.entity.Notice;
import com.lab.util.Pagination;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

/**
 * 站内提醒业务接口。
 */
public interface NoticeService {

    /**
     * 创建提醒（非事务）。
     *
     * @param notice 提醒
     * @return 是否成功
     */
    boolean create(Notice notice);

    /**
     * 事务内创建提醒。
     *
     * @param conn   事务连接
     * @param notice 提醒
     * @return 是否成功
     */
    boolean create(Connection conn, Notice notice);

    /**
     * 分页查询某用户的提醒。
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    Pagination<Notice> findPage(int userId, int pageNum, int pageSize);

    /**
     * 查询某用户最近若干条提醒。
     *
     * @param userId 用户ID
     * @param limit  条数
     * @return 提醒列表
     */
    List<Notice> findByUser(int userId, int limit);

    /**
     * 未读数量。
     *
     * @param userId 用户ID
     * @return 未读条数
     */
    int unreadCount(int userId);

    /**
     * 单条已读。
     *
     * @param id     提醒ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markRead(int id, int userId);

    /**
     * 全部已读。
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllRead(int userId);

    /**
     * 去重判断。
     *
     * @param borrowId 借用记录ID
     * @param type     提醒类型
     * @param from     起始时间
     * @return true 表示已提醒过
     */
    boolean existsRecent(int borrowId, int type, Date from);

    /**
     * 清理历史提醒。
     *
     * @param date 时间阈值
     * @return 删除条数
     */
    int cleanBefore(Date date);
}
