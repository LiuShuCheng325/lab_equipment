package com.lab.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class EncodingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        // 这里只设置字符编码，禁止提前 setContentType("text/html")：
        // 该过滤器映射 /*，会连 /css/、/js/ 等静态资源一起拦截；
        // 一旦提前写死 text/html，DefaultServlet 输出的 text/css 会被覆盖，
        // 浏览器会因 MIME 类型不符拒绝应用样式表，导致全站页面裸奔无样式。
        // 各 Servlet/JSP 均已自行声明正确的 contentType，无需过滤器代劳。
        resp.setCharacterEncoding("UTF-8");
        chain.doFilter(req, resp);
    }
}