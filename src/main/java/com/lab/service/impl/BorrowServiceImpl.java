package com.lab.service.impl;

import com.lab.dao.BorrowRecordDAO;
import com.lab.dao.EquipmentDAO;
import com.lab.dao.impl.BorrowRecordDAOImpl;
import com.lab.dao.impl.EquipmentDAOImpl;
import com.lab.entity.BorrowRecord;
import com.lab.entity.BorrowStat;
import com.lab.entity.DayStat;
import com.lab.entity.Equipment;
import com.lab.entity.Notice;
import com.lab.exception.BusinessException;
import com.lab.exception.DataAccessException;
import com.lab.service.BorrowService;
import com.lab.service.NoticeService;
import com.lab.util.DBUtil;
import com.lab.util.Pagination;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 借用业务实现。
 *
 * <p>事务与并发要点：
 * <ol>
 *   <li>审批/归还全程使用同一条 autoCommit=false 的连接；</li>
 *   <li>先 {@code SELECT ... FOR UPDATE} 锁定借用记录行，再校验状态与库存；</li>
 *   <li>扣库存用 {@code UPDATE ... WHERE available >= ?}，影响行数为 0 即视为库存不足并回滚；</li>
 *   <li>归还用 {@code LEAST(available+?, stock)}，保证不超总库存；</li>
 *   <li>审批备注写入 audit_remark，不再覆盖申请人填写的 remark。</li>
 * </ol>
 */
public class BorrowServiceImpl implements BorrowService {

    /** 单次借用最长天数。 */
    public static final int MAX_BORROW_DAYS = 30;

    /** 即将到期提醒的默认提前天数。 */
    public static final int DEFAULT_DUE_SOON_DAYS = 3;

    private static final long ONE_DAY_MILLIS = 24L * 60 * 60 * 1000;

    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAOImpl();
    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
    private final NoticeService noticeService = new NoticeServiceImpl();

    @Override
    public void add(BorrowRecord record) {
        apply(record);
    }

    @Override
    public int apply(BorrowRecord record) {
        if (record == null) {
            throw new BusinessException("申请信息不能为空");
        }
        if (record.getUserId() == null || record.getEquipmentId() == null) {
            throw new BusinessException("申请人与设备信息不能为空");
        }
        if (record.getQuantity() == null || record.getQuantity() <= 0) {
            throw new BusinessException("借用数量必须大于 0");
        }
        if (record.getBorrowTime() == null || record.getExpectReturnTime() == null) {
            throw new BusinessException("借用时间与预计归还时间不能为空");
        }
        long spanMillis = record.getExpectReturnTime().getTime() - record.getBorrowTime().getTime();
        if (spanMillis <= 0) {
            throw new BusinessException("预计归还时间必须晚于借用时间");
        }
        if (spanMillis > MAX_BORROW_DAYS * ONE_DAY_MILLIS) {
            throw new BusinessException("单次借用时长不能超过 " + MAX_BORROW_DAYS + " 天");
        }

        Connection conn = null;
        try {
            conn = DBUtil.beginTransaction();
            // 事务内重新读取设备，避免读取到已过期的数据
            Equipment equipment = equipmentDAO.findById(conn, record.getEquipmentId());
            if (equipment == null) {
                throw new BusinessException("该设备不存在");
            }
            if (equipment.getStatus() == null || equipment.getStatus() != Equipment.STATUS_NORMAL) {
                throw new BusinessException("该设备当前处于维修/报废状态，无法借用");
            }
            if (equipment.getAvailable() == null || equipment.getAvailable() < record.getQuantity()) {
                throw new BusinessException("库存不足，当前可用：" + equipment.getAvailable());
            }
            if (borrowRecordDAO.existsActive(record.getUserId(), record.getEquipmentId())) {
                throw new BusinessException("您已有一条该设备的待审批或在借记录，请勿重复申请");
            }
            if (record.getStatus() == null) {
                record.setStatus(BorrowRecord.STATUS_PENDING);
            }
            int newId = borrowRecordDAO.add(conn, record);
            if (newId <= 0) {
                throw new BusinessException("提交申请失败，请稍后重试");
            }
            noticeService.create(conn, buildApplyNotice(record));
            DBUtil.commit(conn);
            return newId;
        } catch (BusinessException e) {
            DBUtil.rollback(conn);
            throw e;
        } catch (DataAccessException e) {
            DBUtil.rollback(conn);
            throw new BusinessException("提交申请失败，请稍后重试", e);
        } catch (SQLException e) {
            DBUtil.rollback(conn);
            throw new BusinessException("提交申请失败，请稍后重试", e);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public void audit(int recordId, int result) {
        approve(recordId, result, "", "");
    }

    @Override
    public boolean approve(Integer id, Integer status, String approver, String remark) {
        if (id == null) {
            throw new BusinessException("缺少借用记录ID");
        }
        if (status == null || (status != BorrowRecord.STATUS_APPROVED
                && status != BorrowRecord.STATUS_REJECTED)) {
            throw new BusinessException("审批结果参数不合法");
        }
        Connection conn = null;
        try {
            conn = DBUtil.beginTransaction();
            BorrowRecord record = borrowRecordDAO.findByIdForUpdate(conn, id);
            if (record == null) {
                throw new BusinessException("借用申请不存在");
            }
            if (record.getStatus() == null || record.getStatus() != BorrowRecord.STATUS_PENDING) {
                throw new BusinessException("该申请已被审批，无法重复操作");
            }
            // 通过：先扣库存，库存不足则整单回滚
            if (status == BorrowRecord.STATUS_APPROVED) {
                int rows = equipmentDAO.deductAvailable(conn, record.getEquipmentId(),
                        record.getQuantity() == null ? 0 : record.getQuantity());
                if (rows <= 0) {
                    throw new BusinessException("库存不足，无法通过审批");
                }
            }
            boolean ok = borrowRecordDAO.updateApprove(conn, id, status, approver, remark);
            if (!ok) {
                throw new BusinessException("审批操作失败，请重试");
            }
            noticeService.create(conn, buildAuditNotice(record, status, approver, remark));
            DBUtil.commit(conn);
            return true;
        } catch (BusinessException e) {
            DBUtil.rollback(conn);
            throw e;
        } catch (DataAccessException e) {
            DBUtil.rollback(conn);
            throw new BusinessException("审批操作失败，请稍后重试", e);
        } catch (SQLException e) {
            DBUtil.rollback(conn);
            throw new BusinessException("审批操作失败，请稍后重试", e);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public boolean returnEquipment(Integer id) {
        return returnEquipment(id, BorrowRecord.CONDITION_OK, null);
    }

    @Override
    public boolean returnEquipment(Integer id, Integer returnCondition, String operator) {
        if (id == null) {
            throw new BusinessException("缺少借用记录ID");
        }
        int condition = returnCondition == null ? BorrowRecord.CONDITION_OK : returnCondition;
        if (condition < BorrowRecord.CONDITION_OK || condition > BorrowRecord.CONDITION_LOST) {
            throw new BusinessException("归还状况参数不合法");
        }
        Date now = new Date();
        Connection conn = null;
        try {
            conn = DBUtil.beginTransaction();
            BorrowRecord record = borrowRecordDAO.findByIdForUpdate(conn, id);
            if (record == null) {
                throw new BusinessException("借用记录不存在");
            }
            if (record.getStatus() == null
                    || (record.getStatus() != BorrowRecord.STATUS_APPROVED
                    && record.getStatus() != BorrowRecord.STATUS_OVERDUE)) {
                throw new BusinessException("只有借用中或已逾期的记录才能归还");
            }
            // 归还：库存上界保护
            equipmentDAO.restoreAvailable(conn, record.getEquipmentId(),
                    record.getQuantity() == null ? 0 : record.getQuantity());
            boolean ok = borrowRecordDAO.updateReturn(conn, id,
                    BorrowRecord.STATUS_RETURNED, now, condition);
            if (!ok) {
                throw new BusinessException("归还操作失败，请重试");
            }
            noticeService.create(conn, buildReturnNotice(record, now, condition, operator));
            DBUtil.commit(conn);
            return true;
        } catch (BusinessException e) {
            DBUtil.rollback(conn);
            throw e;
        } catch (DataAccessException e) {
            DBUtil.rollback(conn);
            throw new BusinessException("归还操作失败，请稍后重试", e);
        } catch (SQLException e) {
            DBUtil.rollback(conn);
            throw new BusinessException("归还操作失败，请稍后重试", e);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public boolean remind(Integer id, String operator) {
        if (id == null) {
            throw new BusinessException("缺少借用记录ID");
        }
        Date now = new Date();
        Connection conn = null;
        try {
            conn = DBUtil.beginTransaction();
            BorrowRecord record = borrowRecordDAO.findByIdForUpdate(conn, id);
            if (record == null) {
                throw new BusinessException("借用记录不存在");
            }
            Integer status = record.getStatus();
            if (status == null
                    || (status != BorrowRecord.STATUS_APPROVED
                    && status != BorrowRecord.STATUS_OVERDUE)) {
                throw new BusinessException("只有借用中或已逾期的记录才能催还");
            }
            // 手动催还冷却：与 OverdueServiceImpl 的自动去重保持一致，
            // 同一条记录当天至多催还一次，避免管理员连点刷屏借用同学的提醒中心。
            Date lastRemind = record.getLastRemindTime();
            if (lastRemind != null && isSameDay(lastRemind, now)) {
                throw new BusinessException("今日已对该记录发送过催还提醒，请于明日再试");
            }
            int remindCount = (record.getRemindCount() == null ? 0 : record.getRemindCount()) + 1;
            noticeService.create(conn, buildRemindNotice(record, remindCount, now));
            borrowRecordDAO.updateRemind(conn, id, remindCount, now);
            DBUtil.commit(conn);
            return true;
        } catch (BusinessException e) {
            DBUtil.rollback(conn);
            throw e;
        } catch (DataAccessException e) {
            DBUtil.rollback(conn);
            throw new BusinessException("催还提醒发送失败，请稍后重试", e);
        } catch (SQLException e) {
            DBUtil.rollback(conn);
            throw new BusinessException("催还提醒发送失败，请稍后重试", e);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    /**
     * 判断两个日期是否为同一自然日（用于催还冷却按天去重）。
     *
     * @param date 待比较日期A
     * @param now  待比较日期B
     * @return 同一天返回 true
     */
    private boolean isSameDay(Date date, Date now) {
        if (date == null || now == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        calendar.setTime(date);
        target.setTime(now);
        return calendar.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && calendar.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public Pagination<BorrowRecord> findPage(Integer userId, Integer status, String keyword,
                                             int pageNum, int pageSize) {
        Pagination<BorrowRecord> page = new Pagination<>(pageNum, pageSize);
        int total = borrowRecordDAO.count(userId, status, keyword);
        page.setTotal(total);
        // 页码越界时回退到最后一页，避免出现空白页
        if (total > 0 && page.getStartIndex() >= total) {
            page.setPageNum(page.getTotalPages());
        }
        page.setData(borrowRecordDAO.findPage(userId, status, keyword,
                page.getStartIndex(), page.getPageSize()));
        return page;
    }

    @Override
    public List<BorrowRecord> findByUserId(Integer userId) {
        return borrowRecordDAO.findByUserId(userId);
    }

    @Override
    public List<BorrowRecord> findAll() {
        return borrowRecordDAO.findAll();
    }

    @Override
    public List<BorrowRecord> findPending() {
        return borrowRecordDAO.findPending();
    }

    @Override
    public List<BorrowRecord> findOverdue() {
        return borrowRecordDAO.findOverdue();
    }

    @Override
    public List<BorrowRecord> findOverdueByUser(int userId) {
        return borrowRecordDAO.findOverdueByUser(userId);
    }

    @Override
    public List<BorrowRecord> findDueSoon(int days) {
        return borrowRecordDAO.findDueSoon(days <= 0 ? DEFAULT_DUE_SOON_DAYS : days);
    }

    @Override
    public BorrowRecord findById(Integer id) {
        return borrowRecordDAO.findById(id);
    }

    @Override
    public BorrowStat statistics() {
        return borrowRecordDAO.statistics();
    }

    @Override
    public List<DayStat> borrowTrend(int days) {
        return borrowRecordDAO.borrowTrend(days <= 0 ? 7 : days);
    }

    // ==================== 提醒构造（内部工具） ====================

    /**
     * 构造"申请已提交"提醒，发给申请人。
     */
    private Notice buildApplyNotice(BorrowRecord record) {
        Notice notice = new Notice();
        notice.setUserId(record.getUserId());
        notice.setBorrowId(record.getId());
        notice.setType(Notice.TYPE_SYSTEM);
        notice.setTitle("借用申请已提交");
        notice.setContent("您申请的《" + safeName(record.getEquipmentName()) + "》"
                + (record.getQuantity() == null ? 1 : record.getQuantity()) + " 件已提交，"
                + "预计归还日期 " + formatDate(record.getExpectReturnTime()) + "，请等待管理员审批。");
        notice.setIsRead(0);
        return notice;
    }

    /**
     * 构造"审批结果"提醒，发给申请人。
     */
    private Notice buildAuditNotice(BorrowRecord record, Integer status, String approver, String remark) {
        boolean pass = status != null && status == BorrowRecord.STATUS_APPROVED;
        Notice notice = new Notice();
        notice.setUserId(record.getUserId());
        notice.setBorrowId(record.getId());
        notice.setType(Notice.TYPE_AUDIT);
        notice.setTitle(pass ? "借用申请已通过" : "借用申请被驳回");
        StringBuilder content = new StringBuilder();
        content.append("《").append(safeName(record.getEquipmentName())).append("》借用申请")
                .append(pass ? "已审批通过，请按管理员通知到实验室领取设备，并于 "
                        + formatDate(record.getExpectReturnTime()) + " 前归还。"
                        : "已被驳回。");
        if (remark != null && !remark.isBlank()) {
            content.append("审批意见：").append(remark.trim());
        }
        if (approver != null && !approver.isBlank()) {
            content.append("（审批人：").append(approver).append("）");
        }
        notice.setContent(content.toString());
        notice.setIsRead(0);
        return notice;
    }

    /**
     * 构造"归还完成"提醒，发给借用人。
     */
    private Notice buildReturnNotice(BorrowRecord record, Date returnTime, int condition, String operator) {
        Notice notice = new Notice();
        notice.setUserId(record.getUserId());
        notice.setBorrowId(record.getId());
        notice.setType(Notice.TYPE_SYSTEM);
        notice.setTitle("设备已归还");
        String conditionText = condition == 1 ? "损坏" : (condition == 2 ? "丢失" : "完好");
        StringBuilder content = new StringBuilder();
        content.append("《").append(safeName(record.getEquipmentName())).append("》已于 ")
                .append(formatDate(returnTime)).append(" 归还，设备状况：").append(conditionText).append("。");
        if (record.getExpectReturnTime() != null && returnTime != null
                && returnTime.after(record.getExpectReturnTime())) {
            long days = (returnTime.getTime() - record.getExpectReturnTime().getTime()) / ONE_DAY_MILLIS;
            content.append("本次归还逾期 ").append(days <= 0 ? 1 : days).append(" 天，请注意按时归还。");
        }
        if (operator != null && !operator.isBlank()) {
            content.append("（操作人：").append(operator).append("）");
        }
        notice.setContent(content.toString());
        notice.setIsRead(0);
        return notice;
    }

    /**
     * 构造"管理员催还"提醒，发给借用人。
     *
     * @param record      借用记录
     * @param remindCount 累计提醒次数（含本次）
     * @param now         当前时间
     * @return 提醒对象
     */
    private Notice buildRemindNotice(BorrowRecord record, int remindCount, Date now) {
        Notice notice = new Notice();
        notice.setUserId(record.getUserId());
        notice.setBorrowId(record.getId());
        notice.setType(Notice.TYPE_OVERDUE);
        boolean overdue = record.getExpectReturnTime() != null && now.after(record.getExpectReturnTime());
        long days = 0L;
        if (overdue) {
            days = (now.getTime() - record.getExpectReturnTime().getTime()) / ONE_DAY_MILLIS;
            notice.setTitle("逾期催还提醒");
            notice.setContent("您借用的《" + safeName(record.getEquipmentName()) + "》已逾期 "
                    + (days <= 0 ? 1 : days) + " 天，管理员已第 " + remindCount + " 次提醒，请尽快归还。");
        } else {
            notice.setTitle("归还到期提醒");
            notice.setContent("您借用的《" + safeName(record.getEquipmentName()) + "》即将到期，"
                    + "请于 " + formatDate(record.getExpectReturnTime()) + " 前归还（第 "
                    + remindCount + " 次提醒）。");
        }
        notice.setIsRead(0);
        return notice;
    }

    private String safeName(String value) {
        return value == null || value.isBlank() ? "设备" : value;
    }

    private String formatDate(Date date) {
        return date == null ? "—" : new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
