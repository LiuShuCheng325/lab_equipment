package com.lab.servlet.support;

import com.lab.entity.CategoryStat;
import com.lab.entity.DashboardStat;
import com.lab.entity.DayStat;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 首页统计变量绑定工具：负责把 {@link DashboardStat} 展开到 request 属性。
 *
 * <p>历史变量（{@code equipmentCount} / {@code pendingBorrowCount} /
 * {@code myRepairCount} / {@code pendingRepairCount}）名称保持不变，其余为新增变量。
 */
public final class DashboardSupport {

    private DashboardSupport() {
    }

    /**
     * 把统计结果写入 request 属性。
     *
     * @param request 请求
     * @param stat    统计结果
     * @param userId  当前用户ID（用于未读数）
     * @param unread  当前用户未读提醒数
     */
    public static void bind(HttpServletRequest request, DashboardStat stat,
                            Integer userId, int unread) {
        request.setAttribute("dashboard", stat);
        // —— 历史变量（保持原有语义）——
        request.setAttribute("equipmentCount", stat.getEquipmentNormal());
        request.setAttribute("pendingBorrowCount", stat.getMyPendingCount());
        request.setAttribute("myRepairCount", stat.getMyRepairCount());
        request.setAttribute("pendingRepairCount", stat.getPendingRepairCount());
        // —— 新增变量 ——
        request.setAttribute("equipmentTotal", stat.getEquipmentTotal());
        request.setAttribute("equipmentNormal", stat.getEquipmentNormal());
        request.setAttribute("equipmentAvailable", stat.getEquipmentAvailable());
        request.setAttribute("equipmentRepairing", stat.getEquipmentRepairing());
        request.setAttribute("borrowingCount", stat.getBorrowingCount());
        request.setAttribute("pendingCount", stat.getPendingCount());
        request.setAttribute("returnedCount", stat.getReturnedCount());
        request.setAttribute("rejectedCount", stat.getRejectedCount());
        request.setAttribute("overdueCount", stat.getOverdueCount());
        request.setAttribute("dueSoonCount", stat.getDueSoonCount());
        request.setAttribute("myPendingCount", stat.getMyPendingCount());
        request.setAttribute("myBorrowingCount", stat.getMyBorrowingCount());
        request.setAttribute("myOverdueCount", stat.getMyOverdueCount());
        request.setAttribute("trend", stat.getTrend());
        request.setAttribute("categories", stat.getCategories());
        request.setAttribute("unreadCount", unread);
        request.setAttribute("loginUserId", userId);
    }

    /**
     * 把类别 Map 转成实体列表（页面便于直接遍历）。
     *
     * @param map 类别统计
     * @return 类别统计列表
     */
    public static List<CategoryStat> toCategoryList(Map<String, Integer> map) {
        List<CategoryStat> list = new ArrayList<>();
        if (map == null) {
            return list;
        }
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            list.add(new CategoryStat(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    /**
     * 取趋势最大值，用于页面计算柱高。
     *
     * @param trend 趋势数据
     * @return 最大数量，最小返回 1
     */
    public static int maxTrend(List<DayStat> trend) {
        int max = 1;
        if (trend == null) {
            return max;
        }
        for (DayStat dayStat : trend) {
            if (dayStat != null && dayStat.getCount() > max) {
                max = dayStat.getCount();
            }
        }
        return max;
    }
}
