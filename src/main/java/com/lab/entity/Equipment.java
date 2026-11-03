package com.lab.entity;

import java.util.Date;

public class Equipment {
    private Integer id;
    // 原name → eqName 对应数据库 eq_name
    private String eqName;
    private String model;
    // 原category → eqType 对应数据库 eq_type
    private String eqType;
    // 原quantity → stock 对应数据库 stock
    private Integer stock;
    private Integer available;
    private String location;
    // 数据库字段为 remark，替换原来的 description
    private String remark;
    private Date createTime;
    private Integer status; // 0:正常 1:维修中 2:报废
    // 新增图片字段
    private String img;

    // 关联统计字段（数据库无，由 countBorrowingByEquipment 填充）：当前借出件数
    private Integer borrowCount;

    /** 状态：正常。 */
    public static final int STATUS_NORMAL = 0;

    /** 状态：维修中。 */
    public static final int STATUS_REPAIRING = 1;

    /** 状态：已报废。 */
    public static final int STATUS_SCRAPPED = 2;

    public Equipment() {}

    // 全参构造同步替换字段：description → remark
    public Equipment(Integer id, String eqName, String model, String eqType, Integer stock, Integer available, String location, String remark, Date createTime, Integer status, String img) {
        this.id = id;
        this.eqName = eqName;
        this.model = model;
        this.eqType = eqType;
        this.stock = stock;
        this.available = available;
        this.location = location;
        this.remark = remark;
        this.createTime = createTime;
        this.status = status;
        this.img = img;
    }

    // Getter和Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    // eqName 替代原来name
    public String getEqName() { return eqName; }
    public void setEqName(String eqName) { this.eqName = eqName; }

    // 新增兼容页面 ${equip.name} 的getter
    public String getName() {
        return this.eqName;
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    // eqType 替代原来category
    public String getEqType() { return eqType; }
    public void setEqType(String eqType) { this.eqType = eqType; }

    // stock 替代原来quantity
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getAvailable() { return available; }
    public void setAvailable(Integer available) { this.available = available; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    // remark 替代原description
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    // 新增兼容页面EL表达式的getter：页面${equip.description}会自动调用此方法读取remark
    public String getDescription() {
        return this.remark;
    }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    // 图片get/set
    public String getImg() {
        return img;
    }
    public void setImg(String img) {
        this.img = img;
    }

    // 借出件数 get/set
    public Integer getBorrowCount() {
        return borrowCount == null ? 0 : borrowCount;
    }
    public void setBorrowCount(Integer borrowCount) {
        this.borrowCount = borrowCount;
    }

    /** @return 设备状态中文名称 */
    public String getStatusName() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case STATUS_REPAIRING:
                return "维修中";
            case STATUS_SCRAPPED:
                return "已报废";
            default:
                return "正常";
        }
    }
}