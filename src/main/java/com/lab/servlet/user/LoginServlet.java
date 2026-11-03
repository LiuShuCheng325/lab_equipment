package com.lab.servlet.user;

import com.lab.entity.User;
import com.lab.service.UserService;
import com.lab.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    // 业务层对象，仅实例化一次
    private final UserService userService = new UserServiceImpl();

    /**
     * GET请求：登出操作 + 页面初始化读取记住我Cookie
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 统一编码
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String action = request.getParameter("action");
        // 登出逻辑
        if ("logout".equals(action)) {
            logoutHandle(request, response);
            return;
        }

        // 普通GET访问登录页：读取Cookie回填用户名
        readRememberCookie(request);
        // 转发到登录页面
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    /**
     * POST请求：提交登录表单
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 全局编码统一处理，解决中文乱码
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. 获取前端表单参数并去除首尾空格
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String remember = request.getParameter("remember");

        // null安全trim处理
        username = username == null ? "" : username.trim();
        password = password == null ? "" : password.trim();

        // 简单非空校验
        if (username.isEmpty() || password.isEmpty()) {
            request.setAttribute("error", "用户名和密码不能为空");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        // 2. 调用业务层登录验证（业务层内部完成SHA256密码比对）
        User loginUser = userService.login(username, password);
        if (loginUser != null) {
            // 登录成功：存入Session
            HttpSession session = request.getSession();
            session.setAttribute("user", loginUser);

            // 3. 处理记住我Cookie（传入request修复找不到变量报错）
            handleRememberCookie(username, remember, request, response);

            // 跳转首页（走Servlet加载统计数据）
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            // 登录失败：存入错误提示（前端页面读取 ${error}）
            request.setAttribute("error", "用户名或密码错误，请核对后重试");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    /**
     * 登出处理：销毁Session、清除记住我Cookie
     */
    private void logoutHandle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 销毁会话
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // 清空记住我Cookie
        Cookie clearCookie = new Cookie("loginUser", "");
        clearCookie.setMaxAge(0);
        clearCookie.setPath(request.getContextPath());
        response.addCookie(clearCookie);
        // 重定向登录页
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    /**
     * 处理记住我Cookie：勾选则保存7天，不勾选立即清除
     * 修复：新增request入参，解决107行找不到request符号报错
     */
    private void handleRememberCookie(String username, String remember, HttpServletRequest request, HttpServletResponse response) {
        Cookie userCookie = new Cookie("loginUser", username);
        userCookie.setPath(request.getContextPath());
        // 勾选记住我，有效期7天
        if ("1".equals(remember)) {
            userCookie.setMaxAge(7 * 24 * 60 * 60);
        } else {
            // 不勾选，Cookie立即失效
            userCookie.setMaxAge(0);
        }
        response.addCookie(userCookie);
    }

    /**
     * 读取记住我Cookie，回填用户名到页面
     */
    private void readRememberCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        // 简化冗余判断，消除黄色警告
        if (cookies == null) {
            return;
        }
        for (Cookie cookie : cookies) {
            if ("loginUser".equals(cookie.getName())) {
                String saveUsername = cookie.getValue();
                // 将cookie用户名传递给页面input默认值
                request.setAttribute("saveUsername", saveUsername);
                break;
            }
        }
    }
}