package com.lab.filter;

import jakarta.servlet.*;
import java.io.IOException;

/**
 * 已废弃：编码过滤功能由 EncodingFilter（web.xml 中注册）统一处理。
 * 保留此类仅供查看，不再通过注解自动注册。
 */
public class CharacterEncodingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // 无初始化逻辑，移除未使用的异常声明消除警告
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        // 仅未设置过contentType时才赋值，不覆盖Servlet自定义返回类型
        if(response.getContentType() == null) {
            response.setContentType("text/html;charset=UTF-8");
        }
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 无销毁逻辑
    }
}