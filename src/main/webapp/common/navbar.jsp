<%-- ============================================================
     公共顶栏片段（可复用，放在 .main 内部、.page 之上）
     依赖：ctx（上下文路径）、sessionScope.user、sessionScope.unreadNoticeCount
     可选：activeNav（当前导航标识，用于生成面包屑）、pageTitle（自定义面包屑文案）
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty ctx}">
    <c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
</c:if>
<c:if test="${empty navUser}">
    <c:set var="navUser" value="${sessionScope.user}" scope="page"/>
</c:if>
<c:set var="navUnread" value="0" scope="page"/>
<c:if test="${not empty sessionScope.unreadNoticeCount}">
    <c:set var="navUnread" value="${sessionScope.unreadNoticeCount}" scope="page"/>
</c:if>
<c:choose>
    <c:when test="${activeNav eq 'dashboard'}"><c:set var="navCrumb" value="首页仪表盘" scope="page"/></c:when>
    <c:when test="${activeNav eq 'equipment'}"><c:set var="navCrumb" value="设备一览" scope="page"/></c:when>
    <c:when test="${activeNav eq 'equipment-detail'}"><c:set var="navCrumb" value="设备详情" scope="page"/></c:when>
    <c:when test="${activeNav eq 'borrow-apply'}"><c:set var="navCrumb" value="申请借用" scope="page"/></c:when>
    <c:when test="${activeNav eq 'borrow-my'}"><c:set var="navCrumb" value="我的借用" scope="page"/></c:when>
    <c:when test="${activeNav eq 'borrow-pending'}"><c:set var="navCrumb" value="借用审批" scope="page"/></c:when>
    <c:when test="${activeNav eq 'borrow-all'}"><c:set var="navCrumb" value="借用台账" scope="page"/></c:when>
    <c:when test="${activeNav eq 'borrow-overdue'}"><c:set var="navCrumb" value="逾期与提醒" scope="page"/></c:when>
    <c:when test="${activeNav eq 'repair-my'}"><c:set var="navCrumb" value="我的报修" scope="page"/></c:when>
    <c:when test="${activeNav eq 'repair-add'}"><c:set var="navCrumb" value="提交报修" scope="page"/></c:when>
    <c:when test="${activeNav eq 'repair-all'}"><c:set var="navCrumb" value="报修处理" scope="page"/></c:when>
    <c:when test="${activeNav eq 'notice'}"><c:set var="navCrumb" value="提醒中心" scope="page"/></c:when>
    <c:when test="${activeNav eq 'admin-user'}"><c:set var="navCrumb" value="用户管理" scope="page"/></c:when>
    <c:otherwise>
        <c:set var="navCrumb" value="首页仪表盘" scope="page"/>
        <c:if test="${not empty pageTitle}">
            <c:set var="navCrumb" value="${pageTitle}" scope="page"/>
        </c:if>
    </c:otherwise>
</c:choose>

<header class="topbar">
    <div class="topbar__inner">
        <nav class="crumb">
            <a href="${ctx}/dashboard">首页</a>
            <span class="sep">/</span>
            <span class="current">${fn:escapeXml(navCrumb)}</span>
        </nav>

        <div class="topbar-right">
            <c:if test="${not empty navUser}">
                <a class="icon-btn" href="${ctx}/notice?action=list" title="提醒中心" id="navBell">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                         stroke-linecap="round" stroke-linejoin="round">
                        <path d="M18 8A6 6 0 1 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                    </svg>
                    <c:if test="${navUnread > 0}">
                        <span class="badge-dot" id="navBellBadge">${navUnread}</span>
                    </c:if>
                </a>
            </c:if>

            <c:choose>
                <c:when test="${not empty navUser}">
                    <a class="user-pill" href="${ctx}/borrow?action=myRecords" title="我的借用记录">
                        <span class="avatar avatar--sm">${fn:escapeXml(fn:substring(navUser.name, 0, 1))}</span>
                        <span class="user-pill__meta">
                            <span class="user-pill__name">${fn:escapeXml(navUser.name)}</span>
                            <span class="user-pill__role">${navUser.role == 1 ? '管理员' : '借用用户'}</span>
                        </span>
                    </a>
                    <a class="icon-btn" href="${ctx}/logout" title="退出登录">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                             stroke-linecap="round" stroke-linejoin="round">
                            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                            <path d="m16 17 5-5-5-5"/>
                            <path d="M21 12H9"/>
                        </svg>
                    </a>
                </c:when>
                <c:otherwise>
                    <a class="btn btn--ghost btn--sm" href="${ctx}/login.jsp">登录</a>
                    <a class="btn btn--primary btn--sm" href="${ctx}/register.jsp">注册</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>

<c:if test="${not empty navUser}">
    <script>
        /* 顶栏未读提醒兜底：后端未注入 session 时，异步拉取一次未读数 */
        (function () {
            var badge = document.getElementById('navBellBadge');
            if (badge) {
                return;
            }
            var bell = document.getElementById('navBell');
            if (!bell || typeof fetch !== 'function') {
                return;
            }
            fetch('${ctx}/notice?action=unreadCount', {credentials: 'same-origin'})
                .then(function (res) {
                    return res.ok ? res.text() : null;
                })
                .then(function (text) {
                    var n = parseInt((text || '').trim(), 10);
                    if (!isNaN(n) && n > 0) {
                        var dot = document.createElement('span');
                        dot.className = 'badge-dot';
                        dot.id = 'navBellBadge';
                        dot.textContent = String(n > 99 ? '99+' : n);
                        bell.appendChild(dot);
                    }
                })
                .catch(function () {
                    /* 拉取失败时静默，不影响页面 */
                });
        })();
    </script>
</c:if>
