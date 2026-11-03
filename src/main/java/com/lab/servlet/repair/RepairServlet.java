package com.lab.servlet.repair;

import com.lab.entity.RepairRecord;
import com.lab.entity.User;
import com.lab.service.RepairService;
import com.lab.service.impl.RepairServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 报修记录控制器
 * 访问路径：/repair
 */
@WebServlet("/repair")
public class RepairServlet extends HttpServlet {
    // 业务层实例
    private final RepairService repairService = new RepairServiceImpl();

    // 常量定义，消除魔法数字
    private static final int ADMIN_ROLE = 1; // 管理员角色
    private static final int STATUS_PENDING = 0; // 待处理

    // 页面路径常量统一管理
    private static final String PAGE_LOGIN = "/login.jsp";
    private static final String PAGE_403 = "/error/403.jsp";
    private static final String PAGE_400 = "/error/400.jsp"; // 参数格式错误页
    private static final String PAGE_MY_REPAIRS = "/repair/myRepairs.jsp";
    private static final String PAGE_ALL_REPAIRS = "/repair/allRepairs.jsp";
    private static final String PAGE_REPAIR_APPLY = "/repair/apply.jsp";
    private static final String REDIRECT_MY_REPAIRS = "/repair?action=myRepairs";
    private static final String REDIRECT_ALL_REPAIRS = "/repair?action=allRepairs";

    /**
     * 查询页面、跳转页面、处理报修状态操作 GET 请求
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 统一编码（兜底防乱码）
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. 登录校验
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + PAGE_LOGIN);
            return;
        }

        // 2. 获取action，空值统一兜底为myRepairs
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "myRepairs";
        }
        action = action.trim();

        try {
            switch (action) {
                // 我的报修记录（普通用户/管理员都可访问）
                case "myRepairs":
                    List<RepairRecord> myRepairList = repairService.findByUserId(loginUser.getId());
                    request.setAttribute("recordList", myRepairList);
                    request.getRequestDispatcher(PAGE_MY_REPAIRS).forward(request, response);
                    break;

                // 全部报修记录（仅管理员）
                case "allRepairs":
                    if (!isAdmin(loginUser)) {
                        response.sendRedirect(request.getContextPath() + PAGE_403);
                        return;
                    }
                    List<RepairRecord> allRepairList = repairService.findAll();
                    request.setAttribute("recordList", allRepairList);
                    request.getRequestDispatcher(PAGE_ALL_REPAIRS).forward(request, response);
                    break;

                // 跳转报修申请页面，需要设备id参数
                case "apply":
                    String eqIdParam = request.getParameter("id");
                    if (eqIdParam == null || eqIdParam.trim().isEmpty()) {
                        response.sendRedirect(request.getContextPath() + PAGE_400);
                        return;
                    }
                    int equipmentId = Integer.parseInt(eqIdParam);
                    request.setAttribute("equipmentId", equipmentId);
                    request.getRequestDispatcher(PAGE_REPAIR_APPLY).forward(request, response);
                    break;

                // 管理员处理报修，修改状态
                case "handle":
                    if (!isAdmin(loginUser)) {
                        response.sendRedirect(request.getContextPath() + PAGE_403);
                        return;
                    }
                    String idParam = request.getParameter("id");
                    String statusParam = request.getParameter("status");
                    if (idParam == null || statusParam == null
                            || idParam.trim().isEmpty() || statusParam.trim().isEmpty()) {
                        response.sendRedirect(request.getContextPath() + PAGE_400);
                        return;
                    }
                    int repairId = Integer.parseInt(idParam);
                    int newStatus = Integer.parseInt(statusParam);
                    String remark = request.getParameter("remark");
                    repairService.updateStatus(repairId, newStatus, loginUser.getName(), remark);
                    response.sendRedirect(request.getContextPath() + REDIRECT_ALL_REPAIRS);
                    break;

                // 默认跳转到我的报修
                default:
                    response.sendRedirect(request.getContextPath() + REDIRECT_MY_REPAIRS);
                    break;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace(); // 打印异常，方便开发调试
            response.sendRedirect(request.getContextPath() + PAGE_400);
        }
    }

    /**
     * 提交报修申请 POST 请求
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 统一编码
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. 登录校验
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + PAGE_LOGIN);
            return;
        }

        try {
            // 获取表单参数
            String eqIdParam = request.getParameter("equipmentId");
            String description = request.getParameter("description");

            // 参数非空校验
            if (eqIdParam == null || eqIdParam.trim().isEmpty()
                    || description == null || description.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + PAGE_400);
                return;
            }

            int equipmentId = Integer.parseInt(eqIdParam.trim());
            RepairRecord record = new RepairRecord();
            record.setEquipmentId(equipmentId);
            record.setUserId(loginUser.getId());
            record.setDescription(description.trim());
            record.setStatus(STATUS_PENDING);

            // 新增报修记录
            repairService.add(record);
            // 提交成功，跳转回我的报修列表
            response.sendRedirect(request.getContextPath() + REDIRECT_MY_REPAIRS);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + PAGE_400);
        }
    }

    /**
     * 工具方法：判断当前登录用户是否为管理员
     */
    private boolean isAdmin(User user) {
        return user != null && user.getRole() == ADMIN_ROLE;
    }
}