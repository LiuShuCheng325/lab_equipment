package com.lab.servlet.dashboard;

import com.lab.entity.BorrowRecord;
import com.lab.entity.DashboardStat;
import com.lab.entity.User;
import com.lab.service.BorrowService;
import com.lab.service.DashboardService;
import com.lab.service.NoticeService;
import com.lab.service.OverdueService;
import com.lab.service.impl.BorrowServiceImpl;
import com.lab.service.impl.DashboardServiceImpl;
import com.lab.service.impl.NoticeServiceImpl;
import com.lab.service.impl.OverdueServiceImpl;
import com.lab.servlet.support.DashboardSupport;
import com.lab.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 仪表盘控制器（/dashboard）。
 *
 * <p>提供首页所需的全部统计：设备总数/可借设备数/借用中/待审批/逾期/即将到期/
 * 我的待审批/我的逾期、最近 7 天借用趋势、设备类别分布。
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final DashboardService dashboardService = new DashboardServiceImpl();
    private final BorrowService borrowService = new BorrowServiceImpl();
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

        WebUtil.consumeFlash(request);
        // 进入仪表盘前先刷新一次逾期状态，保证统计准确
        overdueService.scanAll();

        DashboardStat stat = dashboardService.build(user);
        int unread = (user.getId() == null) ? 0 : noticeService.unreadCount(user.getId());
        DashboardSupport.bind(request, stat, user.getId(), unread);

        // 学生端工作台：展示「在借设备卡片」，最多 6 条，按应还日期升序
        boolean isAdmin = user.getRole() != null && user.getRole() == 1;
        if (!isAdmin && user.getId() != null) {
            request.setAttribute("myBorrowList",
                    borrowService.findPage(user.getId(), BorrowRecord.STATUS_APPROVED, "", 1, 6).getData());
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
