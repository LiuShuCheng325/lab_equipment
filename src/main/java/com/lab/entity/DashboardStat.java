package com.lab.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页/仪表盘统计聚合实体。
 */
public class DashboardStat {

    /** 设备总数。 */
    private int equipmentTotal;

    /** 可借设备数（status=0 且 available&gt;0）。 */
    private int equipmentAvailable;

    /** 维修中设备数。 */
    private int equipmentRepairing;

    /** 状态正常设备数（status=0）。 */
    private int equipmentNormal;

    /** 借用中（status=1）。 */
    private int borrowingCount;

    /** 待审批（status=0）。 */
    private int pendingCount;

    /** 已归还（status=3）。 */
    private int returnedCount;

    /** 已驳回（status=2）。 */
    private int rejectedCount;

    /** 已逾期（status=4）。 */
    private int overdueCount;

    /** 即将到期（status=1 且预计归还时间在 3 天内）。 */
    private int dueSoonCount;

    /** 我的待审批。 */
    private int myPendingCount;

    /** 我的借用中。 */
    private int myBorrowingCount;

    /** 我的逾期。 */
    private int myOverdueCount;

    /** 我的报修工单数。 */
    private int myRepairCount;

    /** 待处理报修数（管理员视角）。 */
    private int pendingRepairCount;

    /** 最近 N 天借用趋势。 */
    private List<DayStat> trend = new ArrayList<>();

    /** 设备类别分布。 */
    private List<CategoryStat> categories = new ArrayList<>();

    /** 无参构造。 */
    public DashboardStat() {
    }

    public int getEquipmentTotal() {
        return equipmentTotal;
    }

    public void setEquipmentTotal(int equipmentTotal) {
        this.equipmentTotal = equipmentTotal;
    }

    public int getEquipmentAvailable() {
        return equipmentAvailable;
    }

    public void setEquipmentAvailable(int equipmentAvailable) {
        this.equipmentAvailable = equipmentAvailable;
    }

    public int getEquipmentRepairing() {
        return equipmentRepairing;
    }

    public void setEquipmentRepairing(int equipmentRepairing) {
        this.equipmentRepairing = equipmentRepairing;
    }

    public int getEquipmentNormal() {
        return equipmentNormal;
    }

    public void setEquipmentNormal(int equipmentNormal) {
        this.equipmentNormal = equipmentNormal;
    }

    public int getBorrowingCount() {
        return borrowingCount;
    }

    public void setBorrowingCount(int borrowingCount) {
        this.borrowingCount = borrowingCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getReturnedCount() {
        return returnedCount;
    }

    public void setReturnedCount(int returnedCount) {
        this.returnedCount = returnedCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public int getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(int overdueCount) {
        this.overdueCount = overdueCount;
    }

    public int getDueSoonCount() {
        return dueSoonCount;
    }

    public void setDueSoonCount(int dueSoonCount) {
        this.dueSoonCount = dueSoonCount;
    }

    public int getMyPendingCount() {
        return myPendingCount;
    }

    public void setMyPendingCount(int myPendingCount) {
        this.myPendingCount = myPendingCount;
    }

    public int getMyBorrowingCount() {
        return myBorrowingCount;
    }

    public void setMyBorrowingCount(int myBorrowingCount) {
        this.myBorrowingCount = myBorrowingCount;
    }

    public int getMyOverdueCount() {
        return myOverdueCount;
    }

    public void setMyOverdueCount(int myOverdueCount) {
        this.myOverdueCount = myOverdueCount;
    }

    public int getMyRepairCount() {
        return myRepairCount;
    }

    public void setMyRepairCount(int myRepairCount) {
        this.myRepairCount = myRepairCount;
    }

    public int getPendingRepairCount() {
        return pendingRepairCount;
    }

    public void setPendingRepairCount(int pendingRepairCount) {
        this.pendingRepairCount = pendingRepairCount;
    }

    public List<DayStat> getTrend() {
        return trend;
    }

    public void setTrend(List<DayStat> trend) {
        this.trend = trend == null ? new ArrayList<>() : trend;
    }

    public List<CategoryStat> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryStat> categories) {
        this.categories = categories == null ? new ArrayList<>() : categories;
    }
}
