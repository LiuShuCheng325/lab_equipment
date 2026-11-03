package com.lab.service.impl;

import com.lab.dao.NoticeDAO;
import com.lab.dao.impl.NoticeDAOImpl;
import com.lab.entity.Notice;
import com.lab.service.NoticeService;
import com.lab.util.Pagination;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

/**
 * 站内提醒业务实现。
 */
public class NoticeServiceImpl implements NoticeService {

    /** 标题最大长度。 */
    private static final int MAX_TITLE_LEN = 100;

    /** 内容最大长度。 */
    private static final int MAX_CONTENT_LEN = 450;

    private final NoticeDAO noticeDAO = new NoticeDAOImpl();

    @Override
    public boolean create(Notice notice) {
        normalize(notice);
        return noticeDAO.add(notice);
    }

    @Override
    public boolean create(Connection conn, Notice notice) {
        normalize(notice);
        return noticeDAO.add(conn, notice);
    }

    @Override
    public Pagination<Notice> findPage(int userId, int pageNum, int pageSize) {
        Pagination<Notice> page = new Pagination<>(pageNum, pageSize);
        page.setTotal(noticeDAO.countByUser(userId));
        page.setData(noticeDAO.findByUser(userId, page.getStartIndex(), page.getPageSize()));
        return page;
    }

    @Override
    public List<Notice> findByUser(int userId, int limit) {
        return noticeDAO.findByUser(userId, 0, limit <= 0 ? 10 : limit);
    }

    @Override
    public int unreadCount(int userId) {
        return noticeDAO.countUnread(userId);
    }

    @Override
    public boolean markRead(int id, int userId) {
        return noticeDAO.markRead(id, userId);
    }

    @Override
    public boolean markAllRead(int userId) {
        return noticeDAO.markAllRead(userId);
    }

    @Override
    public boolean existsRecent(int borrowId, int type, Date from) {
        return noticeDAO.existsRecent(borrowId, type, from);
    }

    @Override
    public int cleanBefore(Date date) {
        return noticeDAO.deleteBefore(date);
    }

    /**
     * 入库前统一收敛：去空格 + 长度限制。
     *
     * @param notice 提醒
     */
    private void normalize(Notice notice) {
        if (notice == null) {
            return;
        }
        notice.setTitle(com.lab.util.WebUtil.limit(notice.getTitle(), MAX_TITLE_LEN));
        notice.setContent(com.lab.util.WebUtil.limit(notice.getContent(), MAX_CONTENT_LEN));
        if (notice.getIsRead() == null) {
            notice.setIsRead(0);
        }
        if (notice.getType() == null) {
            notice.setType(Notice.TYPE_SYSTEM);
        }
    }
}
