package com.lab.service;

import com.lab.entity.DashboardStat;
import com.lab.entity.User;

/**
 * 首页/仪表盘统计业务接口。
 */
public interface DashboardService {

    /**
     * 构建首页统计（设备、借用、报修、趋势、类别分布）。
     *
     * @param user 当前登录用户
     * @return 统计聚合结果
     */
    DashboardStat build(User user);
}
