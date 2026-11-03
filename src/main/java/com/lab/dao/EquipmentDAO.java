package com.lab.dao;

import com.lab.entity.Equipment;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * 设备数据访问接口。
 */
public interface EquipmentDAO {

    /**
     * 查询全部设备。
     *
     * @return 设备列表
     */
    List<Equipment> findAll();

    /**
     * 分页 + 条件查询。
     *
     * @param keyword 关键字（名称/型号/位置/备注）
     * @param eqType  类别，空串或 null 表示不限
     * @param status  状态，null 表示不限
     * @param offset  偏移量
     * @param limit   每页条数
     * @return 设备列表
     */
    List<Equipment> findPage(String keyword, String eqType, Integer status, int offset, int limit);

    /**
     * 条件统计。
     *
     * @param keyword 关键字
     * @param eqType  类别
     * @param status  状态
     * @return 条数
     */
    int count(String keyword, String eqType, Integer status);

    /**
     * 按ID查询。
     *
     * @param id 设备ID
     * @return 设备或 null
     */
    Equipment findById(Integer id);

    /**
     * 事务内按ID查询。
     *
     * @param conn 事务连接
     * @param id   设备ID
     * @return 设备或 null
     */
    Equipment findById(Connection conn, Integer id);

    /**
     * 新增设备。
     *
     * @param equipment 设备
     * @return 是否成功
     */
    boolean add(Equipment equipment);

    /**
     * 事务内新增设备。
     *
     * @param conn      事务连接
     * @param equipment 设备
     * @return 是否成功
     */
    boolean add(Connection conn, Equipment equipment);

    /**
     * 更新设备。
     *
     * @param equipment 设备
     * @return 是否成功
     */
    boolean update(Equipment equipment);

    /**
     * 事务内更新设备。
     *
     * @param conn      事务连接
     * @param equipment 设备
     * @return 是否成功
     */
    boolean update(Connection conn, Equipment equipment);

    /**
     * 删除设备。
     *
     * @param id 设备ID
     * @return 是否成功
     */
    boolean delete(Integer id);

    /**
     * 更新可用数量（兼容旧签名）：正数增加（不超过 stock），负数扣减（不为负）。
     *
     * @param id       设备ID
     * @param quantity 变化量
     * @return 是否成功
     */
    boolean updateAvailable(Integer id, Integer quantity);

    /**
     * 审批通过扣减库存：仅当 available &gt;= quantity 时才扣。
     *
     * @param conn     事务连接
     * @param id       设备ID
     * @param quantity 扣减数量
     * @return 影响行数，0 表示库存不足
     */
    int deductAvailable(Connection conn, Integer id, int quantity);

    /**
     * 归还恢复库存：上限为 stock。
     *
     * @param conn     事务连接
     * @param id       设备ID
     * @param quantity 恢复数量
     * @return 影响行数
     */
    int restoreAvailable(Connection conn, Integer id, int quantity);

    /**
     * 查询所有设备类别（去重、去空、排序）。
     *
     * @return 类别列表
     */
    List<String> findCategories();

    /**
     * 统计各设备当前借出件数。
     *
     * @return key=设备ID，value=借出数量
     */
    Map<Integer, Integer> countBorrowingByEquipment();

    /**
     * 统计可借设备数量（status=0 且 available&gt;0）。
     *
     * @return 条数
     */
    int countAvailable();

    /**
     * 统计设备类别分布。
     *
     * @return key=类别，value=数量
     */
    Map<String, Integer> countByCategory();
}
