package com.lab.servlet.user;

import com.lab.entity.User;
import com.lab.service.UserService;
import com.lab.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    // 普通用户角色常量，消除魔术数字
    private static final int ROLE_STUDENT = 0;
    private final UserService userService = new UserServiceImpl();

    /**
     * GET请求：访问注册页面
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 统一编码
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        // 转发到注册页面
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    /**
     * POST请求：提交注册表单
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 全局编码设置，解决中文乱码
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. 获取所有表单参数并去除首尾空格，null安全处理
        String username = trimParam(request.getParameter("username"));
        String password = trimParam(request.getParameter("password"));
        String realName = trimParam(request.getParameter("name"));
        String phone = trimParam(request.getParameter("phone"));
        String email = trimParam(request.getParameter("email"));

        // 2. 前端重复校验兜底（防止绕过JS直接提交非法数据）
        String validateMsg = checkRegisterParam(username, password, realName, phone, email);
        if (validateMsg != null) {
            request.setAttribute("msg", validateMsg);
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // 3. 封装用户实体（密码加密由Service层统一处理，避免重复加密）
        User user = new User();
        user.setUsername(username);
        // 传入明文密码，Service层register()方法内部做SHA256加密
        user.setPassword(password);
        user.setName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(ROLE_STUDENT);

        // 4. 调用业务层执行注册
        boolean registerResult = userService.register(user);
        if (registerResult) {
            // 注册成功：URL编码中文提示，跳转登录页
            String tipText = URLEncoder.encode("注册成功，请登录您的账号", "UTF-8");
            response.sendRedirect(request.getContextPath() + "/login.jsp?success=" + tipText);
        } else {
            // 注册失败：用户名重复，回显注册页
            request.setAttribute("msg", "该用户名已被占用，请更换用户名");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    /**
     * 工具方法：参数去空格，null转为空字符串
     */
    private String trimParam(String param) {
        return param == null ? "" : param.trim();
    }

    /**
     * 后端参数格式校验（兜底校验，弥补JS绕过提交漏洞）
     * @return 校验失败返回提示文本，校验通过返回null
     */
    private String checkRegisterParam(String username, String password, String name, String phone, String email) {
        // 用户名校验：4-16位字母数字
        if (!username.matches("^[a-zA-Z0-9]{4,16}$")) {
            return "用户名格式错误，仅支持4-16位字母与数字";
        }
        // 密码校验：6-20位
        if (!password.matches("^.{6,20}$")) {
            return "密码长度必须为6~20位";
        }
        // 姓名必填
        if (name.isEmpty()) {
            return "真实姓名不能为空";
        }
        // 手机号非空才校验格式
        if (!phone.isEmpty() && !phone.matches("^1[3-9]\\d{9}$")) {
            return "手机号格式不正确，请输入11位有效手机号";
        }
        // 邮箱非空才校验格式
        if (!email.isEmpty() && !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            return "邮箱格式不正确";
        }
        // 全部校验通过
        return null;
    }
}