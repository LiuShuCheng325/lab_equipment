package com.lab.dao;

import com.lab.entity.Notice;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

/**
 * 站内提醒数据访问接口。
 */
public interface NoticeDAO {

    /**
     * 新增提醒（非事务）。
     *
     * @param notice 提醒
     * @return 是否成功
     */
    boolean add(Notice notice);

    /**
     * 事务内新增提醒。
     *
     * @param conn   事务连接
     * @param notice 提醒
     * @return 是否成功
     */
    boolean add(Connection conn, Notice notice);

    /**
     * 查询某用户的提醒列表（按未读优先、时间倒序）。
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit  条数
     * @return 提醒列表
     */
    List<Notice> findByUser(int userId, int offset, int limit);

    /**
     * 统计某用户提醒总数。
     *
     * @param userId 用户ID
     * @return 条数
     */
    int countByUser(int userId);

    /**
     * 统计某用户未读数量。
     *
     * @param userId 用户ID
     * @return 未读条数
     */
    int countUnread(int userId);

    /**
     * 单条已读（带 user_id 条件，防止越权修改他人提醒）。
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
     * 去重判断：同一借用记录 + 同一提醒类型，在 from 之后是否已提醒过。
     *
     * @param borrowId 借用记录ID
     * @param type     提醒类型
     * @param from     起始时间
     * @return true 表示已提醒过
     */
    boolean existsRecent(int borrowId, int type, Date from);

    /**
     * 清理指定时间之前的提醒。
     *
     * @param date 时间阈值
     * @return 删除条数
     */
    int deleteBefore(Date date);
}
