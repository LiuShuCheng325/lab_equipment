package com.lab.filter;

import com.lab.entity.BorrowStat;
import com.lab.entity.User;
import com.lab.service.BorrowService;
import com.lab.service.NoticeService;
import com.lab.service.impl.BorrowServiceImpl;
import com.lab.service.impl.NoticeServiceImpl;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 登录拦截过滤器：未登录访问任何业务页面/接口时跳转登录页。
 *
 * <p>本过滤器只负责"拦截与放行"，不做任何页面内的权限判断；
 * 管理员权限校验统一放在各 Servlet 内部（requireAdmin）。
 * 另外会顺带刷新顶栏铃铛与侧栏红点所需的统计数字。
 */
@WebFilter("/*")
public class LoginFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(LoginFilter.class.getName());

    /** 精确匹配单文件、接口，用 equals。 */
    private static final String[] ALLOW_FILES = {
            "/login.jsp", "/register.jsp", "/login", "/register", "/upload"
    };

    /** 静态资源目录，前缀匹配。 */
    private static final String[] ALLOW_DIRS = {
            "/css/", "/js/", "/images/", "/common/", "/error/", "/upload/"
    };

    private final NoticeService noticeService = new NoticeServiceImpl();
    private final BorrowService borrowService = new BorrowServiceImpl();

    @Override
    public void init(FilterConfig filterConfig) {
        // 无需初始化资源
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getServletPath();

        // 1. 精确放行登录/注册页面、登录接口、图片上传接口
        for (String file : ALLOW_FILES) {
            if (path.equals(file)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // 2. 放行静态资源文件夹、上传图片访问目录
        for (String dir : ALLOW_DIRS) {
            if (path.startsWith(dir)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // 3. 校验登录状态
        HttpSession session = req.getSession();
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        // 4. 刷新徽标统计（失败不影响页面正常访问）
        injectBadges(session, loginUser);

        // 已登录放行其他页面、业务接口
        chain.doFilter(request, response);
    }

    /**
     * 刷新顶栏铃铛未读数与侧栏待办红点。
     *
     * @param session   会话
     * @param loginUser 当前登录用户
     */
    private void injectBadges(HttpSession session, User loginUser) {
        try {
            Integer userId = loginUser.getId();
            if (userId != null) {
                session.setAttribute("unreadNoticeCount", noticeService.unreadCount(userId));
            }
            if (loginUser.getRole() != null && loginUser.getRole() == 1) {
                BorrowStat stat = borrowService.statistics();
                session.setAttribute("pendingCount", stat.getPending());
                session.setAttribute("overdueCount", stat.getOverdue());
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[徽标统计] 刷新失败，不影响页面访问", e);
        }
    }

    @Override
    public void destroy() {
        // 无需释放资源
    }
}
