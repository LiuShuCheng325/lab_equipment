package com.lab.entity;

/**
 * 借用记录统计实体（对应 borrow_record 各状态数量 + 逾期相关统计）。
 */
public class BorrowStat {

    private int total;
    private int pending;
    private int borrowing;
    private int rejected;
    private int returned;
    private int overdue;
    private int dueSoon;

    /** 无参构造。 */
    public BorrowStat() {
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPending() {
        return pending;
    }

    public void setPending(int pending) {
        this.pending = pending;
    }

    public int getBorrowing() {
        return borrowing;
    }

    public void setBorrowing(int borrowing) {
        this.borrowing = borrowing;
    }

    public int getRejected() {
        return rejected;
    }

    public void setRejected(int rejected) {
        this.rejected = rejected;
    }

    public int getReturned() {
        return returned;
    }

    public void setReturned(int returned) {
        this.returned = returned;
    }

    public int getOverdue() {
        return overdue;
    }

    public void setOverdue(int overdue) {
        this.overdue = overdue;
    }

    public int getDueSoon() {
        return dueSoon;
    }

    public void setDueSoon(int dueSoon) {
        this.dueSoon = dueSoon;
    }
}
