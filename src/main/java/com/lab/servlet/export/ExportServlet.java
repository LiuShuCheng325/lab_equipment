package com.lab.servlet.export;

import com.lab.entity.BorrowRecord;
import com.lab.entity.CategoryStat;
import com.lab.entity.DayStat;
import com.lab.entity.Equipment;
import com.lab.entity.User;
import com.lab.service.BorrowService;
import com.lab.service.EquipmentService;
import com.lab.service.impl.BorrowServiceImpl;
import com.lab.service.impl.EquipmentServiceImpl;
import com.lab.util.Pagination;
import com.lab.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 数据导出控制器（/export）。
 *
 * <p>输出 CSV（带 UTF-8 BOM，Excel 打开不乱码），不依赖 POI。
 *
 * <p>type 取值：
 * <ul>
 *   <li>borrow —— 借用台账</li>
 *   <li>equipment —— 设备台账</li>
 *   <li>overdue —— 逾期台账</li>
 *   <li>trend —— 最近 7 天借用趋势</li>
 *   <li>category —— 设备类别分布</li>
 * </ul>
 */
@WebServlet("/export")
public class ExportServlet extends HttpServlet {

    /** 单次导出上限，防止一次导出过大拖垮服务。 */
    private static final int MAX_EXPORT_ROWS = 10000;

    private final BorrowService borrowService = new BorrowServiceImpl();
    private final EquipmentService equipmentService = new EquipmentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String type = WebUtil.str(request, "type", "borrow");
        String keyword = WebUtil.str(request, "keyword", "");
        Integer status = WebUtil.intOrNull(request, "status");
        String eqType = WebUtil.str(request, "eqType", "");

        String fileName = buildFileName(type);
        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''"
                        + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));

        PrintWriter out = response.getWriter();
        // UTF-8 BOM，保证 Excel 正确识别中文
        out.write('\uFEFF');
        writeContent(out, type, user, keyword, status, eqType);
        out.flush();
    }

    /**
     * 根据导出类型写出 CSV 内容。
     *
     * @param out     输出流
     * @param type    导出类型
     * @param user    当前登录用户
     * @param keyword 关键字
     * @param status  状态筛选
     * @param eqType  类别筛选
     */
    private void writeContent(PrintWriter out, String type, User user,
                              String keyword, Integer status, String eqType) {
        if ("equipment".equalsIgnoreCase(type)) {
            Pagination<Equipment> page = equipmentService.findPage(keyword, eqType, status, 1, MAX_EXPORT_ROWS);
            out.println("设备ID,设备名称,型号,类别,总库存,可用数量,存放位置,状态,备注");
            for (Equipment equipment : page.getData()) {
                out.println(joinCsv(
                        str(equipment.getId()),
                        equipment.getEqName(),
                        equipment.getModel(),
                        equipment.getEqType(),
                        str(equipment.getStock()),
                        str(equipment.getAvailable()),
                        equipment.getLocation(),
                        equipment.getStatusName(),
                        equipment.getRemark()));
            }
            return;
        }
        if ("overdue".equalsIgnoreCase(type)) {
            List<BorrowRecord> records = borrowService.findOverdue();
            out.println("记录ID,借用人,设备,数量,借用时间,预计归还,逾期天数,状态");
            for (BorrowRecord record : records) {
                out.println(joinCsv(
                        str(record.getId()),
                        record.getUserName(),
                        record.getEquipmentName(),
                        str(record.getQuantity()),
                        formatDateTime(record.getBorrowTime()),
                        formatDateTime(record.getExpectReturnTime()),
                        str(record.getOverdueDays()) + "天",
                        record.getStatusName()));
            }
            return;
        }
        if ("trend".equalsIgnoreCase(type)) {
            List<DayStat> trend = borrowService.borrowTrend(7);
            out.println("日期,借用申请数");
            for (DayStat dayStat : trend) {
                out.println(joinCsv(dayStat.getDate(), str(dayStat.getCount())));
            }
            return;
        }
        if ("category".equalsIgnoreCase(type)) {
            List<CategoryStat> categories = toCategoryStats(equipmentService.countByCategory());
            out.println("类别,设备数量");
            for (CategoryStat categoryStat : categories) {
                out.println(joinCsv(categoryStat.getName(), str(categoryStat.getCount())));
            }
            return;
        }

        // 默认：借用台账（管理员看全部，学生只看自己的）
        Integer scopeUserId = (isAdmin(user) ? null : user.getId());
        Pagination<BorrowRecord> page =
                borrowService.findPage(scopeUserId, status, keyword, 1, MAX_EXPORT_ROWS);
        out.println("记录ID,借用人,设备,数量,借用时间,预计归还,实际归还,逾期天数,审批人,审批备注,归还状况,状态,申请备注");
        for (BorrowRecord record : page.getData()) {
            out.println(joinCsv(
                    str(record.getId()),
                    record.getUserName(),
                    record.getEquipmentName(),
                    str(record.getQuantity()),
                    formatDateTime(record.getBorrowTime()),
                    formatDateTime(record.getExpectReturnTime()),
                    formatDateTime(record.getActualReturnTime()),
                    str(record.getOverdueDays()) + "天",
                    record.getApprover(),
                    record.getAuditRemark(),
                    record.getReturnConditionName(),
                    record.getStatusName(),
                    record.getRemark()));
        }
    }

    /**
     * 组装导出文件名（带日期）。
     *
     * @param type 导出类型
     * @return 文件名
     */
    private String buildFileName(String type) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String prefix = "borrow_records";
        if ("equipment".equalsIgnoreCase(type)) {
            prefix = "equipment_list";
        } else if ("overdue".equalsIgnoreCase(type)) {
            prefix = "overdue_records";
        } else if ("trend".equalsIgnoreCase(type)) {
            prefix = "borrow_trend";
        } else if ("category".equalsIgnoreCase(type)) {
            prefix = "equipment_category";
        }
        return prefix + "_" + sdf.format(new Date()) + ".csv";
    }

    /**
     * 将类别统计 Map 转为列表（保持顺序）。
     *
     * @param map 类别统计
     * @return 类别统计列表
     */
    private List<CategoryStat> toCategoryStats(Map<String, Integer> map) {
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
     * 按 CSV 规则转义并拼接字段。
     *
     * @param values 字段值
     * @return CSV 行
     */
    private String joinCsv(String... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeCsv(values[i]));
        }
        return sb.toString();
    }

    /**
     * 单个字段转义：包含逗号、引号、换行时用双引号包裹，内部引号翻倍。
     *
     * @param value 原始值
     * @return 转义后的值
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        boolean needQuote = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needQuote ? "\"" + escaped + "\"" : escaped;
    }

    /**
     * Object 转字符串。
     *
     * @param value 值
     * @return 字符串，null 转空串
     */
    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * 格式化日期时间。
     *
     * @param date 日期
     * @return yyyy-MM-dd HH:mm 或空串
     */
    private String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    /**
     * 是否为管理员。
     *
     * @param user 用户
     * @return true 表示管理员
     */
    private boolean isAdmin(User user) {
        return user != null && user.getRole() != null && user.getRole() == 1;
    }
}
