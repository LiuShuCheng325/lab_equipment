package com.lab.service.impl;

import com.lab.dao.EquipmentDAO;
import com.lab.dao.RepairRecordDAO;
import com.lab.dao.impl.EquipmentDAOImpl;
import com.lab.dao.impl.RepairRecordDAOImpl;
import com.lab.entity.Equipment;
import com.lab.entity.RepairRecord;
import com.lab.exception.BusinessException;
import com.lab.exception.DataAccessException;
import com.lab.service.RepairService;
import com.lab.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 报修业务实现。
 *
 * <p>设备状态联动规则（修复了原实现缺失的"处理中"联动）：
 * <ul>
 *   <li>提交报修 → 设备 status=1（维修中）</li>
 *   <li>处理中(1) → 设备 status=1（维修中）</li>
 *   <li>已完成(2) → 设备 status=0（正常），写完成时间</li>
 *   <li>已报废(3) → 设备 status=2（报废），写完成时间</li>
 * </ul>
 * 报修记录更新与设备状态更新在同一事务内完成。
 */
public class RepairServiceImpl implements RepairService {

    private static final Logger LOGGER = Logger.getLogger(RepairServiceImpl.class.getName());

    private final RepairRecordDAO repairRecordDAO = new RepairRecordDAOImpl();
    private final EquipmentDAO equipmentDAO = new EquipmentDAOImpl();

    @Override
    public void add(RepairRecord record) {
        if (record == null) {
            throw new BusinessException("报修信息不能为空");
        }
        Connection conn = null;
        try {
            conn = DBUtil.beginTransaction();
            Equipment equipment = equipmentDAO.findById(conn, record.getEquipmentId());
            if (equipment == null) {
                throw new BusinessException("报修设备不存在");
            }
            equipment.setStatus(Equipment.STATUS_REPAIRING);
            equipmentDAO.update(conn, equipment);
            repairRecordDAO.add(conn, record);
            DBUtil.commit(conn);
        } catch (BusinessException e) {
            DBUtil.rollback(conn);
            throw e;
        } catch (DataAccessException e) {
            DBUtil.rollback(conn);
            throw new BusinessException("提交报修失败，请稍后重试", e);
        } catch (SQLException e) {
            DBUtil.rollback(conn);
            LOGGER.log(Level.SEVERE, "提交报修失败", e);
            throw new BusinessException("提交报修失败，请稍后重试", e);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public boolean updateStatus(Integer id, Integer status, String handler, String remark) {
        if (id == null || status == null) {
            throw new BusinessException("缺少报修记录ID或状态参数");
        }
        Connection conn = null;
        try {
            conn = DBUtil.beginTransaction();
            RepairRecord record = repairRecordDAO.findById(conn, id);
            if (record == null) {
                throw new BusinessException("报修记录不存在");
            }
            record.setStatus(status);
            record.setHandler(handler);
            record.setRemark(remark);

            Integer equipmentStatus;
            if (status == 1) {
                equipmentStatus = Equipment.STATUS_REPAIRING;
            } else if (status == 2) {
                equipmentStatus = Equipment.STATUS_NORMAL;
                record.setCompleteTime(new Date());
            } else if (status == 3) {
                equipmentStatus = Equipment.STATUS_SCRAPPED;
                record.setCompleteTime(new Date());
            } else {
                throw new BusinessException("报修状态参数不合法");
            }

            boolean recordUpdated = repairRecordDAO.update(conn, record);
            if (!recordUpdated) {
                throw new BusinessException("更新报修状态失败，请重试");
            }
            Equipment equipment = equipmentDAO.findById(conn, record.getEquipmentId());
            if (equipment != null) {
                equipment.setStatus(equipmentStatus);
                equipmentDAO.update(conn, equipment);
            }
            DBUtil.commit(conn);
            return true;
        } catch (BusinessException e) {
            DBUtil.rollback(conn);
            throw e;
        } catch (DataAccessException e) {
            DBUtil.rollback(conn);
            throw new BusinessException("更新报修状态失败，请稍后重试", e);
        } catch (SQLException e) {
            DBUtil.rollback(conn);
            LOGGER.log(Level.SEVERE, "更新报修状态失败", e);
            throw new BusinessException("更新报修状态失败，请稍后重试", e);
        } finally {
            DBUtil.closeQuietly(conn);
        }
    }

    @Override
    public List<RepairRecord> findByUserId(Integer userId) {
        return repairRecordDAO.findByUserId(userId);
    }

    @Override
    public List<RepairRecord> findAll() {
        return repairRecordDAO.findAll();
    }

    @Override
    public RepairRecord findById(Integer id) {
        return repairRecordDAO.findById(id);
    }
}
