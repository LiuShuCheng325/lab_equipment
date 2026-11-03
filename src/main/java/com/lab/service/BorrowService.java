package com.lab.service;

import com.lab.entity.BorrowRecord;
import com.lab.entity.BorrowStat;
import com.lab.entity.DayStat;
import com.lab.util.Pagination;

import java.util.List;

/**
 * 借用业务接口。
 *
 * <p>写操作（申请/审批/归还）全部带事务，失败抛出
 * {@link com.lab.exception.BusinessException}，由 Servlet 捕获后转成页面提示。
 */
public interface BorrowService {

    /**
     * 提交借用申请（兼容旧签名）。
     *
     * @param record 借用记录
     */
    void add(BorrowRecord record);

    /**
     * 提交借用申请，返回新记录ID。
     *
     * @param record 借用记录
     * @return 新记录ID
     */
    int apply(BorrowRecord record);

    /**
     * 审批借用申请。
     *
     * @param id      记录ID
     * @param status  审批结果（1=通过，2=驳回）
     * @param approver 审批人
     * @param remark  审批备注（写入 audit_remark，不覆盖申请人备注）
     * @return 是否成功
     */
    boolean approve(Integer id, Integer status, String approver, String remark);

    /**
     * 归还设备（默认完好）。
     *
     * @param id 记录ID
     * @return 是否成功
     */
    boolean returnEquipment(Integer id);

    /**
     * 归还设备。
     *
     * @param id              记录ID
     * @param returnCondition 归还时设备状况（0=完好，1=损坏，2=丢失）
     * @param operator        操作人
     * @return 是否成功
     */
    boolean returnEquipment(Integer id, Integer returnCondition, String operator);

    /**
     * 审批（兼容旧签名，内部复用 approve）。
     *
     * @param recordId 记录ID
     * @param result   审批结果
     */
    /**
     * 管理员发起一次「催还提醒」：给借用人写一条逾期提醒，
     * 并累加 remind_count、刷新 last_remind_time。
     *
     * @param id       借用记录ID
     * @param operator 操作人姓名
     * @return 是否发送成功
     */
    boolean remind(Integer id, String operator);

    void audit(int recordId, int result);

    /**
     * 分页查询借用记录。
     *
     * @param userId  用户ID，null 表示不限
     * @param status  状态，null 表示不限
     * @param keyword 关键字
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    Pagination<BorrowRecord> findPage(Integer userId, Integer status, String keyword,
                                      int pageNum, int pageSize);

    /**
     * 查询某用户借用记录（不分页，兼容旧签名）。
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
     * 查询逾期记录。
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
     * 查询 days 天内即将到期的记录。
     *
     * @param days 天数阈值
     * @return 借用记录列表
     */
    List<BorrowRecord> findDueSoon(int days);

    /**
     * 按ID查询。
     *
     * @param id 记录ID
     * @return 借用记录或 null
     */
    BorrowRecord findById(Integer id);

    /**
     * 借用统计。
     *
     * @return 统计结果
     */
    BorrowStat statistics();

    /**
     * 最近 days 天借用趋势。
     *
     * @param days 天数
     * @return 按天聚合结果
     */
    List<DayStat> borrowTrend(int days);
}
