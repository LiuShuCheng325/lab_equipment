package com.lab.entity;

import java.util.Date;

/**
 * 站内提醒实体，对应 notice 表。
 *
 * <p>type 取值：1=即将到期，2=逾期，3=审批结果，4=系统
 */
public class Notice {

    /** 提醒类型：即将到期。 */
    public static final int TYPE_DUE_SOON = 1;

    /** 提醒类型：逾期。 */
    public static final int TYPE_OVERDUE = 2;

    /** 提醒类型：审批结果。 */
    public static final int TYPE_AUDIT = 3;

    /** 提醒类型：系统。 */
    public static final int TYPE_SYSTEM = 4;

    private Integer id;
    private Integer userId;
    private Integer borrowId;
    private Integer type;
    private String title;
    private String content;
    private Integer isRead;
    private Date createTime;

    /** 无参构造。 */
    public Notice() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Integer borrowId) {
        this.borrowId = borrowId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /** @return 类型的中文名称，便于页面直接展示 */
    public String getTypeName() {
        if (type == null) {
            return "系统";
        }
        switch (type) {
            case TYPE_DUE_SOON:
                return "即将到期";
            case TYPE_OVERDUE:
                return "逾期提醒";
            case TYPE_AUDIT:
                return "审批结果";
            default:
                return "系统通知";
        }
    }
}
