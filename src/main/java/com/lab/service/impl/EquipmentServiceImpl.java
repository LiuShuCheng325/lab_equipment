package com.lab.service.impl;

import com.lab.dao.BorrowRecordDAO;
import com.lab.dao.EquipmentDAO;
import com.lab.dao.impl.BorrowRecordDAOImpl;
import com.lab.dao.impl.EquipmentDAOImpl;
import com.lab.entity.Equipment;
import com.lab.exception.BusinessException;
import com.lab.service.EquipmentService;
import com.lab.util.Pagination;
import com.lab.util.WebUtil;

import java.util.List;
import java.util.Map;

/**
 * 设备业务实现。
 */
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAOImpl();

    @Override
    public List<Equipment> findAll() {
        return equipmentDAO.findAll();
    }

    @Override
    public Pagination<Equipment> findPage(String keyword, String eqType, Integer status,
                                          int pageNum, int pageSize) {
        Pagination<Equipment> page = new Pagination<>(pageNum, pageSize);
        int total = equipmentDAO.count(keyword, eqType, status);
        page.setTotal(total);
        if (total > 0 && page.getStartIndex() >= total) {
            page.setPageNum(page.getTotalPages());
        }
        page.setData(equipmentDAO.findPage(keyword, eqType, status,
                page.getStartIndex(), page.getPageSize()));
        return page;
    }

    @Override
    public Equipment findById(Integer id) {
        return equipmentDAO.findById(id);
    }

    @Override
    public boolean add(Equipment equipment) {
        validate(equipment);
        return equipmentDAO.add(equipment);
    }

    @Override
    public boolean update(Equipment equipment) {
        validate(equipment);
        return equipmentDAO.update(equipment);
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null) {
            throw new BusinessException("缺少设备ID");
        }
        Equipment equipment = equipmentDAO.findById(id);
        if (equipment == null) {
            throw new BusinessException("设备不存在或已被删除");
        }
        // 存在未结束的借用记录（待审批/借用中/逾期）时拒绝删除
        if (borrowRecordDAO.countActiveByEquipment(id) > 0) {
            throw new BusinessException("该设备仍有关联的借用记录（待审批/借用中/逾期），无法删除");
        }
        return equipmentDAO.delete(id);
    }

    @Override
    public boolean updateAvailable(Integer id, Integer quantity) {
        return equipmentDAO.updateAvailable(id, quantity);
    }

    @Override
    public List<String> findCategories() {
        return equipmentDAO.findCategories();
    }

    @Override
    public Map<Integer, Integer> countBorrowingByEquipment() {
        return equipmentDAO.countBorrowingByEquipment();
    }

    @Override
    public int countAvailable() {
        return equipmentDAO.countAvailable();
    }

    @Override
    public Map<String, Integer> countByCategory() {
        return equipmentDAO.countByCategory();
    }

    /**
     * 设备入参校验：名称非空、库存非负、可用量区间合法。
     *
     * @param equipment 设备
     */
    private void validate(Equipment equipment) {
        if (equipment == null) {
            throw new BusinessException("设备信息不能为空");
        }
        equipment.setEqName(WebUtil.limit(equipment.getEqName(), 100));
        equipment.setModel(WebUtil.limit(equipment.getModel(), 100));
        equipment.setEqType(WebUtil.limit(equipment.getEqType(), 50));
        equipment.setLocation(WebUtil.limit(equipment.getLocation(), 200));
        equipment.setRemark(WebUtil.limit(equipment.getRemark(), 500));
        if (equipment.getEqName() == null || equipment.getEqName().trim().isEmpty()) {
            throw new BusinessException("设备名称不能为空");
        }
        if (equipment.getStock() == null || equipment.getStock() < 0) {
            throw new BusinessException("总库存必须是不小于 0 的整数");
        }
        if (equipment.getAvailable() == null) {
            equipment.setAvailable(equipment.getStock());
        }
        if (equipment.getAvailable() < 0) {
            throw new BusinessException("可用数量不能为负数");
        }
        if (equipment.getAvailable() > equipment.getStock()) {
            throw new BusinessException("可用数量不能超过总库存");
        }
        if (equipment.getStatus() == null) {
            equipment.setStatus(Equipment.STATUS_NORMAL);
        }
    }

}
