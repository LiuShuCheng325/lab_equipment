package com.lab.service.impl;

import com.lab.entity.BorrowRecord;
import com.lab.entity.BorrowStat;
import com.lab.entity.CategoryStat;
import com.lab.entity.DashboardStat;
import com.lab.entity.DayStat;
import com.lab.entity.Equipment;
import com.lab.entity.RepairRecord;
import com.lab.entity.User;
import com.lab.service.BorrowService;
import com.lab.service.DashboardService;
import com.lab.service.EquipmentService;
import com.lab.service.RepairService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 首页统计业务实现。
 */
public class DashboardServiceImpl implements DashboardService {

    /** 趋势统计默认天数。 */
    private static final int DEFAULT_TREND_DAYS = 7;

    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final BorrowService borrowService = new BorrowServiceImpl();
    private final RepairService repairService = new RepairServiceImpl();

    @Override
    public DashboardStat build(User user) {
        DashboardStat stat = new DashboardStat();
        if (user == null) {
            return stat;
        }

        // -------- 设备维度 --------
        List<Equipment> allEquipment = equipmentService.findAll();
        int normalCount = 0;
        int repairingCount = 0;
        int scrappedCount = 0;
        for (Equipment equipment : allEquipment) {
            Integer status = equipment.getStatus();
            if (status == null || status == Equipment.STATUS_NORMAL) {
                normalCount++;
            } else if (status == Equipment.STATUS_REPAIRING) {
                repairingCount++;
            } else {
                scrappedCount++;
            }
        }
        stat.setEquipmentTotal(allEquipment.size());
        stat.setEquipmentAvailable(equipmentService.countAvailable());
        stat.setEquipmentRepairing(repairingCount);
        stat.setEquipmentNormal(normalCount);

        // -------- 借用维度 --------
        BorrowStat borrowStat = borrowService.statistics();
        stat.setPendingCount(borrowStat.getPending());
        stat.setBorrowingCount(borrowStat.getBorrowing());
        stat.setReturnedCount(borrowStat.getReturned());
        stat.setRejectedCount(borrowStat.getRejected());
        stat.setOverdueCount(borrowStat.getOverdue());
        stat.setDueSoonCount(borrowStat.getDueSoon());

        // -------- 个人维度 --------
        Integer myId = user.getId();
        if (myId != null) {
            stat.setMyPendingCount(borrowService.findPage(myId, BorrowRecord.STATUS_PENDING, "", 1, 1).getTotal());
            stat.setMyBorrowingCount(borrowService.findPage(myId, BorrowRecord.STATUS_APPROVED, "", 1, 1).getTotal());
            stat.setMyOverdueCount(borrowService.findPage(myId, BorrowRecord.STATUS_OVERDUE, "", 1, 1).getTotal());
            List<RepairRecord> myRepairs = repairService.findByUserId(myId);
            stat.setMyRepairCount(myRepairs == null ? 0 : myRepairs.size());
        }

        // -------- 管理员维度 --------
        if (user.getRole() != null && user.getRole() == 1) {
            List<RepairRecord> allRepairs = repairService.findAll();
            int pendingRepair = 0;
            if (allRepairs != null) {
                for (RepairRecord repair : allRepairs) {
                    if (repair.getStatus() != null && repair.getStatus() == 0) {
                        pendingRepair++;
                    }
                }
            }
            stat.setPendingRepairCount(pendingRepair);
        }

        // -------- 图表数据 --------
        List<DayStat> trend = borrowService.borrowTrend(DEFAULT_TREND_DAYS);
        stat.setTrend(trend == null ? new ArrayList<>() : trend);

        Map<String, Integer> categoryMap = equipmentService.countByCategory();
        List<CategoryStat> categories = new ArrayList<>();
        if (categoryMap != null) {
            for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
                categories.add(new CategoryStat(entry.getKey(), entry.getValue()));
            }
        }
        stat.setCategories(categories);

        return stat;
    }
}
