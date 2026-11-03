package com.lab.entity;

/**
 * 设备类别分布统计实体（用于首页"设备类别分布"）。
 */
public class CategoryStat {

    /** 类别名称。 */
    private String name;

    /** 该类别下设备数量。 */
    private int count;

    /** 无参构造。 */
    public CategoryStat() {
    }

    /**
     * @param name  类别名称
     * @param count 数量
     */
    public CategoryStat(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
