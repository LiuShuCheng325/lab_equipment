package com.lab.dao;

import com.lab.entity.BorrowRecord;
import com.lab.entity.BorrowStat;
import com.lab.entity.DayStat;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 借用记录数据访问接口。
 *
 * <p>约定：不带 {@link Connection} 的方法自己获取/关闭连接（自动提交）；
 * 带 {@link Connection} 的重载方法由调用方（Service 事务）负责提交、回滚与关闭。
 */
public interface BorrowRecordDAO {

    /**
     * 新增借用记录（兼容旧签名）。
     *
     * @param record 借用记录
     */
    void add(BorrowRecord record);

    /**
     * 新增借用记录并返回自增主键。
     *
     * @param record 借用记录
     * @return 新记录主键，0 表示失败
     */
    int addReturnId(BorrowRecord record);

    /**
     * 事务内新增借用记录。
     *
     * @param conn   事务连接
     * @param record 借用记录
     * @return 新记录主键，0 表示失败
     */
    int add(Connection conn, BorrowRecord record);

    /**
     * 整行更新（兼容旧签名）。
     *
     * @param record 借用记录
     */
    void update(BorrowRecord record);

    /**
     * 事务内整行更新。
     *
     * @param conn   事务连接
     * @param record 借用记录
     * @return 是否更新成功
     */
    boolean update(Connection conn, BorrowRecord record);

    /**
     * 审批：只有待审批(status=0)的记录可被更新，审批备注写入 audit_remark，
     * 不会覆盖申请人填写的 remark。
     *
     * @param conn        事务连接
     * @param id          记录ID
     * @param status      目标状态
     * @param approver    审批人
     * @param auditRemark 审批备注
     * @return 是否更新成功
     */
    boolean updateApprove(Connection conn, Integer id, Integer status,
                          String approver, String auditRemark);

    /**
     * 归还：写入状态、实际归还时间与设备状况。
     *
     * @param conn            事务连接
     * @param id              记录ID
     * @param status          目标状态
     * @param returnTime      实际归还时间
     * @param returnCondition 归还时设备状况
     * @return 是否更新成功
     */
    boolean updateReturn(Connection conn, Integer id, Integer status,
                         Date returnTime, Integer returnCondition);

    /**
     * 仅更新状态（非事务）。
     *
     * @param id     记录ID
     * @param status 目标状态
     * @return 是否更新成功
     */
    boolean updateStatus(Integer id, Integer status);

    /**
     * 仅更新状态（事务）。
     *
     * @param conn   事务连接
     * @param id     记录ID
     * @param status 目标状态
     * @return 是否更新成功
     */
    boolean updateStatus(Connection conn, Integer id, Integer status);

    /**
     * 更新提醒计数与最近提醒时间。
     *
     * @param conn           事务连接
     * @param id             记录ID
     * @param remindCount    累计提醒次数
     * @param lastRemindTime 最近提醒时间
     * @return 是否更新成功
     */
    boolean updateRemind(Connection conn, Integer id, int remindCount, Date lastRemindTime);

    /**
     * 按ID查询。
     *
     * @param id 记录ID
     * @return 借用记录或 null
     */
    BorrowRecord findById(Integer id);

    /**
     * 事务内按ID查询。
     *
     * @param conn 事务连接
     * @param id   记录ID
     * @return 借用记录或 null
     */
    BorrowRecord findById(Connection conn, Integer id);

    /**
     * 事务内按ID查询并加行锁（SELECT ... FOR UPDATE）。
     *
     * @param conn 事务连接
     * @param id   记录ID
     * @return 借用记录或 null
     */
    BorrowRecord findByIdForUpdate(Connection conn, Integer id);

    /**
     * 查询某用户的借用记录。
     *
     * @param userId 用户ID
     * @return 借用记录列表
     */
    List<BorrowRecord> findByUserId(Integer userId);

    /**
     * 查询全部借用记录。
     *
     * @return 借用记录列表
     */
    List<BorrowRecord> findAll();

    /**
     * 查询待审批记录。
     *
     * @return 借用记录列表
     */
    List<BorrowRecord> findPending();

    /**
     * 查询逾期记录（含 status=4 已逾期 与 status=1 但已过期）。
     *
     * @return 借用记录列表
     */
    List<BorrowRecord> findOverdue();

    /**
     * 查询某用户的逾期记录。
     *
     * @param userId 用户ID
     * @return 借用记录列表
     */
    List<BorrowRecord> findOverdueByUser(int userId);

    /**
     * 查询 days 天内即将到期的借用中记录。
     *
     * @param days 天数阈值
     * @return 借用记录列表
     */
    List<BorrowRecord> findDueSoon(int days);

    /**
     * 查询指定状态且预计归还时间早于 time 的记录。
     *
     * @param status 状态
     * @param time   时间阈值
     * @return 借用记录列表
     */
    List<BorrowRecord> findByStatusAndDueBefore(int status, Date time);

    /**
     * 判断同一用户对同一设备是否已有未结束借用（待审批/借用中/逾期）。
     *
     * @param userId      用户ID
     * @param equipmentId 设备ID
     * @return true 表示存在重复申请
     */
    boolean existsActive(int userId, int equipmentId);

    /**
     * 分页查询。
     *
     * @param userId  用户ID，null 表示不限
     * @param status  状态，null 表示不限
     * @param keyword 关键字（设备名/用户名/备注），空串表示不限
     * @param offset  偏移量
     * @param limit   每页条数
     * @return 借用记录列表
     */
    List<BorrowRecord> findPage(Integer userId, Integer status, String keyword, int offset, int limit);

    /**
     * 统计符合条件总数。
     *
     * @param userId  用户ID，null 表示不限
     * @param status  状态，null 表示不限
     * @param keyword 关键字，空串表示不限
     * @return 记录条数
     */
    int count(Integer userId, Integer status, String keyword);

    /**
     * 借用记录统计（各状态数量 + 逾期 + 即将到期）。
     *
     * @return 统计结果
     */
    BorrowStat statistics();

    /**
     * 统计指定设备当前未结束的借用记录数（待审批/借用中/逾期）。
     *
     * @param equipmentId 设备ID
     * @return 记录条数
     */
    int countActiveByEquipment(Integer equipmentId);

    /**
     * 统计各设备当前借出件数。
     *
     * @return key=设备ID，value=借出数量
     */
    Map<Integer, Integer> countBorrowingByEquipment();

    /**
     * 最近 days 天借用申请趋势。
     *
     * @param days 天数
     * @return 按天聚合结果
     */
    List<DayStat> borrowTrend(int days);
}
