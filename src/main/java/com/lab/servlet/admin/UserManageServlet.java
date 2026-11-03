package com.lab.servlet.admin;

import com.lab.entity.User;
import com.lab.service.UserService;
import com.lab.service.impl.UserServiceImpl;
import com.lab.util.Pagination;
import com.lab.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// 匹配侧边栏链接 /admin/user
@WebServlet("/admin/user")
public class UserManageServlet extends HttpServlet {

    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 校验登录管理员权限
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null || loginUser.getRole() != 1) {
            response.sendRedirect(request.getContextPath() + "/error/403.jsp");
            return;
        }

        // 取出一次性提示（删除/保存结果），写入 request 供 toast 展示
        WebUtil.consumeFlash(request);

        String action = request.getParameter("action");
        if (action == null) action = "list";

        int id;
        switch (action) {
            case "list":
                // 新增：支持关键字搜索与分页（keyword / pageNum），不传时退化为第一页全量
                String keyword = WebUtil.str(request, "keyword", "");
                int pageNum = WebUtil.intParam(request, "pageNum", 1);
                int pageSize = WebUtil.intParam(request, "pageSize", Pagination.DEFAULT_PAGE_SIZE);
                Pagination<User> page = userService.findPage(keyword, pageNum, pageSize);
                request.setAttribute("userList", page.getData());
                request.setAttribute("pageNum", page.getPageNum());
                request.setAttribute("pageTotal", page.getTotalPages());
                request.setAttribute("total", page.getTotal());
                request.setAttribute("keyword", keyword);
                // 转发到管理员用户列表页面
                request.getRequestDispatcher("/admin/list.jsp").forward(request, response);
                break;
            case "delete":
                id = Integer.parseInt(request.getParameter("id"));
                userService.delete(id);
                response.sendRedirect(request.getContextPath() + "/admin/user");
                break;
            case "edit":
                id = Integer.parseInt(request.getParameter("id"));
                User editUser = userService.findById(id);
                request.setAttribute("editUser", editUser);
                request.getRequestDispatcher("/admin/edit.jsp").forward(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/admin/user");
        }
    }

    // 补充throws ServletException，规范写法
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null || loginUser.getRole() != 1) {
            response.sendRedirect(request.getContextPath() + "/error/403.jsp");
            return;
        }

        // 新增：删除升级为 POST 提交（action=delete），与列表页的 POST 表单保持一致；
        // 未携带 action 或 action=update 时沿用原有「更新资料」逻辑，保证向后兼容。
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "update";
        }
        if ("delete".equals(action)) {
            int deleteId = Integer.parseInt(request.getParameter("id"));
            if (loginUser.getId() != null && loginUser.getId().equals(deleteId)) {
                WebUtil.flashErr(request.getSession(), "不能删除当前登录的账号");
            } else {
                userService.delete(deleteId);
                WebUtil.flashMsg(request.getSession(), "账号已删除");
            }
            response.sendRedirect(request.getContextPath() + "/admin/user");
            return;
        }

        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        int role = Integer.parseInt(request.getParameter("role"));

        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setName(name);
        updateUser.setPhone(phone);
        updateUser.setEmail(email);
        updateUser.setRole(role);

        userService.update(updateUser);
        response.sendRedirect(request.getContextPath() + "/admin/user");
    }
}