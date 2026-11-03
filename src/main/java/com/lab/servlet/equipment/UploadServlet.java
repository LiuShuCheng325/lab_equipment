package com.lab.servlet.equipment;
import com.lab.util.UploadUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;

@WebServlet("/upload")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class UploadServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=utf-8");
        // 默认返回空字符串，无图片/上传失败则img为空，数据库存空
        String imgPath = "";
        try {
            // 前端FormData键名是 uploadImg，这里同步修改
            Part part = request.getPart("uploadImg");
            // 判断文件不为空
            if (part != null && part.getSize() > 0) {
                imgPath = UploadUtil.uploadImage(part, request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 输出图片相对路径给前端隐藏域赋值
        response.getWriter().write(imgPath);
    }
}