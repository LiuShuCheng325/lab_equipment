package com.lab.servlet.equipment;

import com.lab.entity.Equipment;
import com.lab.entity.User;
import com.lab.service.EquipmentService;
import com.lab.service.impl.EquipmentServiceImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/test/addEq")
public class TestAddServlet extends HttpServlet {
    private final EquipmentService equipmentService = new EquipmentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        // 仅允许管理员访问，防止测试接口暴露给普通用户
        User loginUser = (User) request.getSession().getAttribute("user");
        if (loginUser == null || loginUser.getRole() != 1) {
            response.sendRedirect(request.getContextPath() + "/error/403.jsp");
            return;
        }
        PrintWriter out = response.getWriter();
        try {
            // 硬编码完整测试数据，匹配数据库全部字段
            Equipment eq = new Equipment();
            eq.setEqName("测试超级计算机");
            eq.setModel("TEST-2026-X");
            eq.setEqType("计算机设备");
            eq.setStock(2);
            eq.setAvailable(2);
            eq.setLocation("博识楼3302A");
            eq.setRemark("临时测试新增设备");
            eq.setImg("");
            eq.setStatus(0);

            equipmentService.add(eq);
            out.println("<h2 style='color:green'>✅ 数据库插入成功！刷新设备列表即可看到数据</h2>");
        } catch (Exception e) {
            out.println("<h2 style='color:red'>❌ 新增失败，异常详情：</h2>");
            e.printStackTrace(out);
        }
    }
}