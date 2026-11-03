package com.lab.servlet.notice;

import com.lab.entity.Notice;
import com.lab.entity.User;
import com.lab.service.NoticeService;
import com.lab.service.impl.NoticeServiceImpl;
import com.lab.util.Pagination;
import com.lab.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 站内提醒控制器（/notice）。
 *
 * <p>action：
 * <ul>
 *   <li>list —— 我的提醒列表（分页）</li>
 *   <li>read —— 单条已读（参数 id）</li>
 *   <li>readAll —— 全部已读</li>
 *   <li>unreadCount / count —— 未读数量（返回 JSON）</li>
 * </ul>
 */
@WebServlet("/notice")
public class NoticeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NoticeServlet.class.getName());

    private final NoticeService noticeService = new NoticeServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = WebUtil.str(request, "action", "list");
        try {
            switch (action) {
                case "unreadCount":
                case "count":
                    writeJson(response, "{\"unreadCount\":" + noticeService.unreadCount(user.getId()) + "}");
                    return;
                case "read":
                    Integer id = WebUtil.intOrNull(request, "id");
                    if (id != null) {
                        noticeService.markRead(id, user.getId());
                    }
                    response.sendRedirect(request.getContextPath() + "/notice?action=list");
                    return;
                case "readAll":
                    noticeService.markAllRead(user.getId());
                    response.sendRedirect(request.getContextPath() + "/notice?action=list");
                    return;
                case "list":
                default:
                    showList(request, response, user);
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "提醒模块请求处理失败，action=" + action, e);
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 提醒的已读操作允许 POST（页面可改用表单提交），逻辑与 GET 一致
        doGet(request, response);
    }

    /**
     * 展示提醒列表。
     *
     * @param request  请求
     * @param response 响应
     * @param user     当前登录用户
     * @throws ServletException 转发失败
     * @throws IOException      转发失败
     */
    private void showList(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        int pageNum = WebUtil.intParam(request, "pageNum", 1);
        int pageSize = WebUtil.intParam(request, "pageSize", 20);
        Pagination<Notice> page = noticeService.findPage(user.getId(), pageNum, pageSize);
        List<Notice> noticeList = page.getData();

        request.setAttribute("noticeList", noticeList);
        request.setAttribute("recordList", noticeList); // 兼容通用列表页面命名
        request.setAttribute("pageNum", page.getPageNum());
        request.setAttribute("pageTotal", page.getTotalPages());
        request.setAttribute("pageSize", page.getPageSize());
        request.setAttribute("total", page.getTotal());
        request.setAttribute("unreadCount", noticeService.unreadCount(user.getId()));
        request.getRequestDispatcher("/notice/list.jsp").forward(request, response);
    }

    /**
     * 输出 JSON 响应。
     *
     * @param response 响应
     * @param json    JSON 文本
     * @throws IOException 写出失败
     */
    private void writeJson(HttpServletResponse response, String json) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
    }
}
