package com.lab.servlet.borrow;

import com.lab.entity.BorrowRecord;
import com.lab.entity.Equipment;
import com.lab.entity.User;
import com.lab.exception.BusinessException;
import com.lab.service.BorrowService;
import com.lab.service.EquipmentService;
import com.lab.service.NoticeService;
import com.lab.service.OverdueService;
import com.lab.service.impl.BorrowServiceImpl;
import com.lab.service.impl.EquipmentServiceImpl;
import com.lab.service.impl.NoticeServiceImpl;
import com.lab.service.impl.OverdueServiceImpl;
import com.lab.util.Pagination;
import com.lab.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 借用记录控制器（/borrow）。
 *
 * <p>URL 与 action 完全向后兼容：
 * 只读 action（myRecords / allRecords / pending / overdue / apply）走 GET 并支持
 * pageNum / pageSize / keyword / status；
 * 写操作 action（add / audit / return）走 <b>POST</b>，
 * 旧的 GET 写操作链接不再执行写入，仅给出友好提示并跳回列表。
 *
 * <p>提示统一采用 <b>session flash</b>：写操作重定向前写入 session，
 * 下一次 GET 读取后写入 request 的 {@code msg} / {@code errMsg} 并清除。
 */
@WebServlet("/borrow")
public class BorrowServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BorrowServlet.class.getName());

    /** 日期格式：yyyy-MM-dd。 */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /** 一天的毫秒数。 */
    private static final long ONE_DAY = 24L * 60 * 60 * 1000;

    private final BorrowService borrowService = new BorrowServiceImpl();
    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final OverdueService overdueService = new OverdueServiceImpl();
    private final NoticeService noticeService = new NoticeServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // 取出上一次写操作留下的一次性提示
        WebUtil.consumeFlash(request);

        String action = WebUtil.str(request, "action", "myRecords");
        String keyword = WebUtil.str(request, "keyword", "");
        Integer statusParam = WebUtil.intOrNull(request, "status");
        int pageNum = WebUtil.intParam(request, "pageNum", 1);
        int pageSize = WebUtil.intParam(request, "pageSize", Pagination.DEFAULT_PAGE_SIZE);

        try {
            switch (action) {
                case "myRecords":
                    forwardPage(request, response, "myRecords",
                            borrowService.findPage(user.getId(), statusParam, keyword, pageNum, pageSize),
                            keyword, statusParam, user);
                    break;
                case "allRecords":
                    requireAdmin(user);
                    forwardPage(request, response, "allRecords",
                            borrowService.findPage(null, statusParam, keyword, pageNum, pageSize),
                            keyword, statusParam, user);
                    break;
                case "pending":
                    requireAdmin(user);
                    // pending 固定只看待审批，忽略 URL 上的 status
                    forwardPage(request, response, "pending",
                            borrowService.findPage(null, BorrowRecord.STATUS_PENDING, keyword, pageNum, pageSize),
                            keyword, BorrowRecord.STATUS_PENDING, user);
                    break;
                case "overdue":
                    requireAdmin(user);
                    // 每次访问逾期页先做一次逾期扫描，保证状态与提醒是最新的
                    overdueService.scanOverdue();
                    forwardPage(request, response, "overdue",
                            borrowService.findPage(null, BorrowRecord.STATUS_OVERDUE, keyword, pageNum, pageSize),
                            keyword, BorrowRecord.STATUS_OVERDUE, user);
                    break;
                case "apply":
                    showApplyPage(request, response, user);
                    break;
                case "audit":
                    // GET 不再执行审批，仅提示使用 POST
                    WebUtil.flashErr(request.getSession(), "审批操作已升级为 POST 提交，请使用列表页中的按钮完成审批");
                    response.sendRedirect(request.getContextPath() + "/borrow?action=pending");
                    break;
                case "return":
                    // GET 不再执行归还，仅提示使用 POST
                    WebUtil.flashErr(request.getSession(), "归还操作已升级为 POST 提交，请使用列表页中的按钮完成归还");
                    response.sendRedirect(request.getContextPath() + "/borrow?action=myRecords");
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/borrow?action=myRecords");
                    break;
            }
        } catch (BusinessException e) {
            WebUtil.flashErr(request.getSession(), e.getMessage());
            response.sendRedirect(request.getContextPath() + "/borrow?action=myRecords");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "借用模块请求处理失败，action=" + action, e);
            WebUtil.flashErr(request.getSession(), "操作失败，请稍后重试");
            response.sendRedirect(request.getContextPath() + "/borrow?action=myRecords");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = WebUtil.str(request, "action", "");
        try {
            switch (action) {
                case "add":
                    handleApply(request, response, user);
                    break;
                case "audit":
                    handleAudit(request, response, user);
                    break;
                case "return":
                    handleReturn(request, response, user);
                    break;
                case "remind":
                    handleRemind(request, response, user);
                    break;
                default:
                    WebUtil.flashErr(request.getSession(), "未知的操作类型：" + action);
                    response.sendRedirect(request.getContextPath() + "/borrow?action=myRecords");
                    break;
            }
        } catch (BusinessException e) {
            WebUtil.flashErr(request.getSession(), e.getMessage());
            response.sendRedirect(request.getContextPath() + "/borrow?action=myRecords");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "借用模块写操作失败，action=" + action, e);
            WebUtil.flashErr(request.getSession(), "操作失败，请稍后重试");
            response.sendRedirect(request.getContextPath() + "/borrow?action=myRecords");
        }
    }

    // ==================== 页面渲染 ====================

    /**
     * 统一设置分页与统计变量后转发到对应 JSP。
     *
     * @param request  请求
     * @param response 响应
     * @param action   当前 action（用于回跳与高亮）
     * @param page     分页结果
     * @param keyword  关键字
     * @param status   状态筛选
     * @param user     当前登录用户
     * @throws ServletException 转发失败
     * @throws IOException      转发失败
     */
    private void forwardPage(HttpServletRequest request, HttpServletResponse response, String action,
                             Pagination<BorrowRecord> page, String keyword, Integer status, User user)
            throws ServletException, IOException {
        // 变量名保持与现有 JSP 一致：recordList / pageNum / pageTotal
        request.setAttribute("recordList", page.getData());
        request.setAttribute("pageNum", page.getPageNum());
        request.setAttribute("pageTotal", page.getTotalPages());
        // 追加变量（不影响现有页面）
        request.setAttribute("pageSize", page.getPageSize());
        request.setAttribute("total", page.getTotal());
        request.setAttribute("keyword", keyword);
        request.setAttribute("status", status);
        request.setAttribute("stat", borrowService.statistics());
        request.setAttribute("action", action);
        // 「我的」维度统计：供「我的借用」页顶部四张小卡使用（新增，不影响已有变量）
        if (user != null && user.getId() != null) {
            request.setAttribute("myPendingCount",
                    borrowService.findPage(user.getId(), BorrowRecord.STATUS_PENDING, "", 1, 1).getTotal());
            request.setAttribute("myBorrowingCount",
                    borrowService.findPage(user.getId(), BorrowRecord.STATUS_APPROVED, "", 1, 1).getTotal());
            request.setAttribute("myReturnedCount",
                    borrowService.findPage(user.getId(), BorrowRecord.STATUS_RETURNED, "", 1, 1).getTotal());
            request.setAttribute("myOverdueCount",
                    borrowService.findPage(user.getId(), BorrowRecord.STATUS_OVERDUE, "", 1, 1).getTotal());
        }
        if (user != null && user.getId() != null) {
            request.setAttribute("unreadCount", noticeService.unreadCount(user.getId()));
        }
        request.getRequestDispatcher("/borrow/" + action + ".jsp").forward(request, response);
    }

    /**
     * 展示借用申请页面。
     *
     * @param request  请求
     * @param response 响应
     * @param user     当前登录用户
     * @throws ServletException 转发失败
     * @throws IOException      转发失败
     */
    private void showApplyPage(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        Integer equipmentId = WebUtil.intOrNull(request, "id");
        if (equipmentId == null) {
            WebUtil.flashErr(request.getSession(), "缺少设备ID，无法发起借用申请");
            response.sendRedirect(request.getContextPath() + "/equipment?action=list");
            return;
        }
        Equipment equipment = equipmentService.findById(equipmentId);
        if (equipment == null) {
            WebUtil.flashErr(request.getSession(), "该设备不存在");
            response.sendRedirect(request.getContextPath() + "/equipment?action=list");
            return;
        }
        if (equipment.getStatus() == null || equipment.getStatus() != Equipment.STATUS_NORMAL) {
            WebUtil.flashErr(request.getSession(), "该设备当前处于维修/报废状态，无法借用");
            response.sendRedirect(request.getContextPath() + "/equipment?action=list");
            return;
        }
        if (equipment.getAvailable() == null || equipment.getAvailable() <= 0) {
            WebUtil.flashErr(request.getSession(), "该设备当前无可借库存");
            response.sendRedirect(request.getContextPath() + "/equipment?action=list");
            return;
        }
        request.setAttribute("equipment", equipment);
        request.getRequestDispatcher("/borrow/apply.jsp").forward(request, response);
    }

    // ==================== 写操作 ====================

    /**
     * 提交借用申请。
     *
     * @param request  请求
     * @param response 响应
     * @param user     当前登录用户
     * @throws IOException 重定向失败
     */
    private void handleApply(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String ctx = request.getContextPath();
        Integer equipmentId = WebUtil.intOrNull(request, "equipmentId");
        int quantity = WebUtil.intParam(request, "borrowNum", 0);
        String borrowDateStr = WebUtil.str(request, "borrowDate", "");
        String expectReturnDateStr = WebUtil.str(request, "expectReturnDate", "");
        String remark = WebUtil.limit(request.getParameter("remark"), 500);

        if (equipmentId == null) {
            WebUtil.flashErr(request.getSession(), "缺少设备ID，无法提交申请");
            response.sendRedirect(ctx + "/equipment?action=list");
            return;
        }
        if (quantity <= 0) {
            WebUtil.flashErr(request.getSession(), "借用数量必须大于 0");
            response.sendRedirect(ctx + "/borrow?action=apply&id=" + equipmentId);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        sdf.setLenient(false);
        Date borrowTime = new Date();
        Date expectReturnTime = new Date(System.currentTimeMillis() + 7L * ONE_DAY);
        try {
            if (!borrowDateStr.isEmpty()) {
                borrowTime = sdf.parse(borrowDateStr);
            }
            if (!expectReturnDateStr.isEmpty()) {
                expectReturnTime = sdf.parse(expectReturnDateStr);
            }
        } catch (ParseException e) {
            WebUtil.flashErr(request.getSession(), "日期格式错误，请使用 yyyy-MM-dd");
            response.sendRedirect(ctx + "/borrow?action=apply&id=" + equipmentId);
            return;
        }

        BorrowRecord record = new BorrowRecord();
        record.setUserId(user.getId());
        record.setEquipmentId(equipmentId);
        record.setQuantity(quantity);
        record.setStatus(BorrowRecord.STATUS_PENDING);
        record.setBorrowTime(borrowTime);
        record.setExpectReturnTime(expectReturnTime);
        record.setRemark(remark);

        try {
            borrowService.apply(record);
            WebUtil.flashMsg(request.getSession(), "借用申请已提交，请等待管理员审批");
        } catch (BusinessException e) {
            WebUtil.flashErr(request.getSession(), e.getMessage());
        }
        response.sendRedirect(ctx + "/borrow?action=myRecords");
    }

    /**
     * 审批借用申请（POST）。
     *
     * @param request  请求
     * @param response 响应
     * @param user     当前登录用户
     * @throws IOException 重定向失败
     */
    private void handleAudit(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String ctx = request.getContextPath();
        if (!isAdmin(user)) {
            response.sendRedirect(ctx + "/error/403.jsp");
            return;
        }
        Integer id = WebUtil.intOrNull(request, "id");
        Integer result = WebUtil.intOrNull(request, "result");
        String remark = WebUtil.limit(request.getParameter("remark"), 255);

        if (id == null || result == null) {
            WebUtil.flashErr(request.getSession(), "缺少审批参数");
            response.sendRedirect(ctx + "/borrow?action=pending");
            return;
        }

        try {
            borrowService.approve(id, result, user.getName(), remark);
            WebUtil.flashMsg(request.getSession(),
                    result == BorrowRecord.STATUS_APPROVED ? "审批通过，库存已扣减" : "已驳回该借用申请");
        } catch (BusinessException e) {
            WebUtil.flashErr(request.getSession(), e.getMessage());
        }
        response.sendRedirect(ctx + "/borrow?action=pending");
    }

    /**
     * 归还设备（POST，支持 returnCondition）。
     *
     * @param request  请求
     * @param response 响应
     * @param user     当前登录用户
     * @throws IOException 重定向失败
     */
    private void handleReturn(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String ctx = request.getContextPath();
        Integer id = WebUtil.intOrNull(request, "id");
        Integer returnCondition = WebUtil.intOrNull(request, "returnCondition");
        if (id == null) {
            WebUtil.flashErr(request.getSession(), "缺少借用记录ID");
            response.sendRedirect(ctx + "/borrow?action=myRecords");
            return;
        }
        if (returnCondition == null) {
            returnCondition = BorrowRecord.CONDITION_OK;
        }

        try {
            borrowService.returnEquipment(id, returnCondition, user.getName());
            WebUtil.flashMsg(request.getSession(), "归还成功，感谢配合");
        } catch (BusinessException e) {
            WebUtil.flashErr(request.getSession(), e.getMessage());
        }
        response.sendRedirect(ctx + "/borrow?action=myRecords");
    }

    /**
     * 处理管理员「催还提醒」（POST /borrow?action=remind）。
     *
     * @param request  请求
     * @param response 响应
     * @param user     当前登录用户
     * @throws IOException 重定向失败
     */
    private void handleRemind(HttpServletRequest request, HttpServletResponse response, User user)
            throws IOException {
        String ctx = request.getContextPath();
        Integer id = WebUtil.intOrNull(request, "id");
        try {
            requireAdmin(user);
            if (id == null) {
                throw new BusinessException("缺少借用记录ID");
            }
            borrowService.remind(id, user.getName());
            WebUtil.flashMsg(request.getSession(), "催还提醒已发送给借用人");
        } catch (BusinessException e) {
            WebUtil.flashErr(request.getSession(), e.getMessage());
        }
        response.sendRedirect(ctx + "/borrow?action=overdue");
    }

    // ==================== 工具方法 ====================

    /**
     * 管理员校验。
     *
     * @param user 当前登录用户
     */
    private void requireAdmin(User user) {
        if (!isAdmin(user)) {
            throw new BusinessException("无权限访问该页面");
        }
    }

    /**
     * 判断是否为管理员。
     *
     * @param user 用户
     * @return true 表示管理员
     */
    private boolean isAdmin(User user) {
        return user != null && user.getRole() != null && user.getRole() == 1;
    }
}
