package com.lab.dao;

import com.lab.entity.RepairRecord;

import java.sql.Connection;
import java.util.List;

/**
 * 报修记录数据访问接口。
 */
public interface RepairRecordDAO {

    /**
     * 新增报修记录。
     *
     * @param record 报修记录
     */
    void add(RepairRecord record);

    /**
     * 事务内新增报修记录。
     *
     * @param conn   事务连接
     * @param record 报修记录
     */
    void add(Connection conn, RepairRecord record);

    /**
     * 更新报修记录（状态/完成时间/处理人/备注）。
     *
     * @param record 报修记录
     */
    void update(RepairRecord record);

    /**
     * 事务内更新报修记录。
     *
     * @param conn   事务连接
     * @param record 报修记录
     * @return 是否更新成功
     */
    boolean update(Connection conn, RepairRecord record);

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

    /**
     * 事务内按ID查询。
     *
     * @param conn 事务连接
     * @param id   记录ID
     * @return 报修记录或 null
     */
    RepairRecord findById(Connection conn, Integer id);
}
