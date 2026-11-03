package com.lab.service;

import com.lab.entity.Equipment;
import com.lab.util.Pagination;

import java.util.List;
import java.util.Map;

/**
 * 设备业务接口。
 */
public interface EquipmentService {

    /**
     * 查询全部设备。
     *
     * @return 设备列表
     */
    List<Equipment> findAll();

    /**
     * 分页 + 条件查询。
     *
     * @param keyword  关键字
     * @param eqType   类别
     * @param status   状态
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    Pagination<Equipment> findPage(String keyword, String eqType, Integer status,
                                   int pageNum, int pageSize);

    /**
     * 按ID查询。
     *
     * @param id 设备ID
     * @return 设备或 null
     */
    Equipment findById(Integer id);

    /**
     * 新增设备。
     *
     * @param equipment 设备
     * @return 是否成功
     */
    boolean add(Equipment equipment);

    /**
     * 更新设备。
     *
     * @param equipment 设备
     * @return 是否成功
     */
    boolean update(Equipment equipment);

    /**
     * 删除设备（存在未结束借用记录时拒绝删除并抛出业务异常）。
     *
     * @param id 设备ID
     * @return 是否成功
     */
    boolean delete(Integer id);

    /**
     * 更新设备可用数量。
     *
     * @param id       设备ID
     * @param quantity 变化量
     * @return 是否成功
     */
    boolean updateAvailable(Integer id, Integer quantity);

    /**
     * 查询所有设备类别。
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
     * 统计可借设备数量。
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
