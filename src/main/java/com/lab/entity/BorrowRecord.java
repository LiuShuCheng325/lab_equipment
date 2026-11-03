package com.lab.entity;

import java.util.Date;

public class BorrowRecord {

    /** 状态：待审批。 */
    public static final int STATUS_PENDING = 0;

    /** 状态：已通过（借用中）。 */
    public static final int STATUS_APPROVED = 1;

    /** 状态：已驳回。 */
    public static final int STATUS_REJECTED = 2;

    /** 状态：已归还。 */
    public static final int STATUS_RETURNED = 3;

    /** 状态：已逾期。 */
    public static final int STATUS_OVERDUE = 4;

    /** 归还状况：完好。 */
    public static final int CONDITION_OK = 0;

    /** 归还状况：损坏。 */
    public static final int CONDITION_DAMAGED = 1;

    /** 归还状况：丢失。 */
    public static final int CONDITION_LOST = 2;

    private Integer id;
    private Integer userId;
    private Integer equipmentId;
    private Integer quantity;
    private String remark;
    private Integer status;
    private String approver;

    // 新增借用时间、预计归还时间
    private Date borrowTime;
    private Date expectReturnTime;

    private Date actualReturnTime;

    /** 审批备注（审批人填写，与申请人 remark 分离，避免覆盖学生备注）。 */
    private String auditRemark;

    /** 归还时设备状况：0=完好，1=损坏，2=丢失。 */
    private Integer returnCondition;

    /** 已提醒次数。 */
    private Integer remindCount;

    /** 最近一次提醒时间。 */
    private Date lastRemindTime;

    // 逾期天数（页面计算展示，数据库不存）
    private Integer overdueDays;

    // 关联查询展示字段（数据库无，仅页面显示）
    private String userName;
    private String userPhone;
    private String equipmentName;

    // ==================== Get & Set ====================
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

    public Integer getEquipmentId() {
        return equipmentId;
    }
    public void setEquipmentId(Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getApprover() {
        return approver;
    }
    public void setApprover(String approver) {
        this.approver = approver;
    }

    // ========== 新增 borrowTime ==========
    public Date getBorrowTime() {
        return borrowTime;
    }
    public void setBorrowTime(Date borrowTime) {
        this.borrowTime = borrowTime;
    }

    // ========== 新增 expectReturnTime ==========
    public Date getExpectReturnTime() {
        return expectReturnTime;
    }
    public void setExpectReturnTime(Date expectReturnTime) {
        this.expectReturnTime = expectReturnTime;
    }

    public Date getActualReturnTime() {
        return actualReturnTime;
    }
    public void setActualReturnTime(Date actualReturnTime) {
        this.actualReturnTime = actualReturnTime;
    }

    // ========== 审批备注 auditRemark ==========
    public String getAuditRemark() {
        return auditRemark;
    }
    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    // ========== 归还设备状况 returnCondition ==========
    public Integer getReturnCondition() {
        return returnCondition;
    }
    public void setReturnCondition(Integer returnCondition) {
        this.returnCondition = returnCondition;
    }

    /** @return 归还状况中文名称 */
    public String getReturnConditionName() {
        if (returnCondition == null) {
            return "—";
        }
        switch (returnCondition) {
            case CONDITION_DAMAGED:
                return "损坏";
            case CONDITION_LOST:
                return "丢失";
            default:
                return "完好";
        }
    }

    // ========== 提醒次数 remindCount ==========
    public Integer getRemindCount() {
        return remindCount;
    }
    public void setRemindCount(Integer remindCount) {
        this.remindCount = remindCount;
    }

    // ========== 最近提醒时间 lastRemindTime ==========
    public Date getLastRemindTime() {
        return lastRemindTime;
    }
    public void setLastRemindTime(Date lastRemindTime) {
        this.lastRemindTime = lastRemindTime;
    }

    // ========== 逾期天数 overdueDays ==========
    public Integer getOverdueDays() {
        return overdueDays;
    }
    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }

    /** @return 状态中文名称，页面可直接 ${record.statusName} */
    public String getStatusName() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case STATUS_PENDING:
                return "待审批";
            case STATUS_APPROVED:
                return "已通过";
            case STATUS_REJECTED:
                return "已驳回";
            case STATUS_RETURNED:
                return "已归还";
            case STATUS_OVERDUE:
                return "已逾期";
            default:
                return "未知";
        }
    }

    // 关联用户名
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    // 关联借用人手机号（逾期中心催还用）
    public String getUserPhone() {
        return userPhone;
    }
    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    // 关联设备名
    public String getEquipmentName() {
        return equipmentName;
    }
    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }
}