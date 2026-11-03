package com.lab.service.impl;

import com.lab.dao.BorrowRecordDAO;
import com.lab.dao.NoticeDAO;
import com.lab.dao.impl.BorrowRecordDAOImpl;
import com.lab.dao.impl.NoticeDAOImpl;
import com.lab.entity.BorrowRecord;
import com.lab.entity.Notice;
import com.lab.exception.DataAccessException;
import com.lab.service.OverdueService;
import com.lab.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 逾期扫描业务实现。
 *
 * <p>并发要点：扫描与审批共用同一张表，所有状态变更都在事务内完成，
 * 且先 {@code SELECT ... FOR UPDATE} 再更新，避免扫描把"刚被归还"的记录改回逾期。
 */
public class OverdueServiceImpl implements OverdueService {

    private static final Logger LOGGER = Logger.getLogger(OverdueServiceImpl.class.getName());

    /** 默认即将到期阈值（天）。 */
    private static final int DEFAULT_DUE_SOON_DAYS = 3;

    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAOImpl();
    private final NoticeDAO noticeDAO = new NoticeDAOImpl();

    @Override
    public int scanOverdue() {
        List<BorrowRecord> candidates =
                borrowRecordDAO.findByStatusAndDueBefore(BorrowRecord.STATUS_APPROVED, new Date());
        if (candidates.isEmpty()) {
            return 0;
        }
        Date todayStart = startOfToday();
        int changed = 0;
        Connection conn = null;
        try {
            conn = DBUtil.beginTransaction();
            for (BorrowRecord candidate : candidates) {
                BorrowRecord locked = borrowRecordDAO.findByIdForUpdate(conn, candidate.getId());
                if (locked == null || locked.getStatus() == null
                        || locked.getStatus() != BorrowRecord.STATUS_APPROVED) {
                    continue;
                }
                if (!borrowRecordDAO.updateStatus(conn, locked.getId(), BorrowRecord.STATUS_OVERDUE)) {
                    continue;
                }
                changed++;
                // 去重：同一借用记录 + 逾期类型，当天只提醒一次
                if (!noticeDAO.existsRecent(locked.getId(), Notice.TYPE_OVERDUE, todayStart)) {
                    noticeDAO.add(conn, buildNotice(locked, Notice.TYPE_OVERDUE));
                    borrowRecordDAO.updateRemind(conn, locked.getId(),
                            safeRemindCount(locked) + 1, new Date());
                }
            }
            DBUtil.commit(conn);
        } catch (SQLException e) {
            DBUtil.rollback(conn);
            LOGGER.log(Level.SEVERE, "逾期扫描失败（数据库异常）", e);
            throw new DataAccessException("逾期扫描失败", e);
        } catch (DataAccessException e) {
            DBUtil.rollback(conn);
            throw e;
        } finally {
            DBUtil.closeQuietly(conn);
        }
        return changed;
    }

    @Override
    public int scanDueSoon(int days) {
        int withinDays = days <= 0 ? DEFAULT_DUE_SOON_DAYS : days;
        List<BorrowRecord> records = borrowRecordDAO.findDueSoon(withinDays);
        if (records.isEmpty()) {
            return 0;
        }
        Date todayStart = startOfToday();
        int added = 0;
        Connection conn = null;
        try {
            conn = DBUtil.beginTransaction();
            for (BorrowRecord record : records) {
                if (!noticeDAO.existsRecent(record.getId(), Notice.TYPE_DUE_SOON, todayStart)) {
                    noticeDAO.add(conn, buildNotice(record, Notice.TYPE_DUE_SOON));
                    borrowRecordDAO.updateRemind(conn, record.getId(),
                            safeRemindCount(record) + 1, new Date());
                    added++;
                }
            }
            DBUtil.commit(conn);
        } catch (SQLException e) {
            DBUtil.rollback(conn);
            LOGGER.log(Level.SEVERE, "即将到期扫描失败（数据库异常）", e);
            throw new DataAccessException("即将到期扫描失败", e);
        } catch (DataAccessException e) {
            DBUtil.rollback(conn);
            throw e;
        } finally {
            DBUtil.closeQuietly(conn);
        }
        return added;
    }

    @Override
    public int scanAll() {
        int overdue = scanOverdue();
        scanDueSoon(DEFAULT_DUE_SOON_DAYS);
        return overdue;
    }

    /**
     * 根据借用记录与提醒类型构造一条提醒。
     *
     * @param record 借用记录
     * @param type   提醒类型
     * @return 提醒实体
     */
    private Notice buildNotice(BorrowRecord record, int type) {
        Notice notice = new Notice();
        notice.setUserId(record.getUserId());
        notice.setBorrowId(record.getId());
        notice.setType(type);
        String equipmentName = record.getEquipmentName() == null ? "设备" : record.getEquipmentName();
        if (type == Notice.TYPE_OVERDUE) {
            notice.setTitle("借用逾期提醒");
            notice.setContent("您借用的「" + equipmentName + "」已超过预计归还时间，请尽快归还！");
        } else {
            notice.setTitle("即将到期提醒");
            notice.setContent("您借用的「" + equipmentName + "」将在 " + DEFAULT_DUE_SOON_DAYS
                    + " 天内到期，请提前安排归还。");
        }
        return notice;
    }

    /**
     * 安全读取提醒次数。
     *
     * @param record 借用记录
     * @return 提醒次数
     */
    private int safeRemindCount(BorrowRecord record) {
        return record.getRemindCount() == null ? 0 : record.getRemindCount();
    }

    /**
     * 当天 00:00:00，用于提醒去重。
     *
     * @return 当天起始时间
     */
    private Date startOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
