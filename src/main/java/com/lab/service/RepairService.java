package com.lab.service;

import com.lab.entity.RepairRecord;

import java.util.List;

/**
 * 报修业务接口。
 */
public interface RepairService {

    /**
     * 提交报修申请。
     *
     * @param record 报修记录
     */
    void add(RepairRecord record);

    /**
     * 处理报修（更新状态，并联动设备状态）。
     *
     * @param id      记录ID
     * @param status  目标状态（1=处理中，2=已完成，3=已报废）
     * @param handler 处理人
     * @param remark  处理备注
     * @return 是否成功
     */
    boolean updateStatus(Integer id, Integer status, String handler, String remark);

    /**
     * 查询某用户的报修记录。
     *
     * @param userId 用户ID
     * @return 报修记录列表
     */
    List<RepairRecord> findByUserId(Integer userId);

    /**
     * 查询全部报修记录。
     *
     * @return 报修记录列表
     */
    List<RepairRecord> findAll();

    /**
     * 按ID查询。
     *
     * @param id 记录ID
     * @return 报修记录或 null
     */
    RepairRecord findById(Integer id);
}
