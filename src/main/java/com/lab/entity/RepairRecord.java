package com.lab.entity;

import java.util.Date;

public class RepairRecord {
    private Integer id;
    private Integer equipmentId;
    private Integer userId;
    private String description;
    private Date applyTime;
    private Date completeTime;
    private Integer status;
    private String handler;
    private String remark;



    // 新增：设备图片路径字段
    private String imgUrl;

    // 关联查询字段
    private String equipmentName;
    private String userName;

    public RepairRecord() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Integer equipmentId) { this.equipmentId = equipmentId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getApplyTime() { return applyTime; }
    public void setApplyTime(Date applyTime) { this.applyTime = applyTime; }

    public Date getCompleteTime() { return completeTime; }
    public void setCompleteTime(Date completeTime) { this.completeTime = completeTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    // 新增图片路径 Get/Set 方法
    public String getImgUrl() {
        return imgUrl;
    }
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    // 关联字段getter/setter
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}