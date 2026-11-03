package com.lab.entity;

/**
 * 按天聚合的统计实体（用于首页"最近 N 天借用趋势"小柱状图）。
 */
public class DayStat {

    /** 日期，格式 yyyy-MM-dd。 */
    private String date;

    /** 当天数量。 */
    private int count;

    /** 无参构造。 */
    public DayStat() {
    }

    /**
     * @param date  日期
     * @param count 数量
     */
    public DayStat(String date, int count) {
        this.date = date;
        this.count = count;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
