package com.lab.service;

/**
 * 逾期扫描业务接口。
 *
 * <p>负责把"借用中但已过期"的记录落库为"已逾期"(status=4)，
 * 并生成逾期提醒与即将到期提醒（同一借用记录 + 同一提醒类型，当天只提醒一次）。
 */
public interface OverdueService {

    /**
     * 扫描逾期：status=1 且 expect_return_time &lt; NOW() 的记录置为 status=4，并写入逾期提醒。
     *
     * @return 本次被置为逾期的记录数
     */
    int scanOverdue();

    /**
     * 扫描即将到期：status=1 且预计归还时间在 days 天内，写入即将到期提醒。
     *
     * @param days 天数阈值，小于等于 0 时按 3 天处理
     * @return 本次新增提醒数
     */
    int scanDueSoon(int days);

    /**
     * 先扫描逾期再扫描即将到期。
     *
     * @return 逾期记录数
     */
    int scanAll();
}
