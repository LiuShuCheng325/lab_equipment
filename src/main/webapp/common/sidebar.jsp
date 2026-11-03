<%-- ============================================================
     公共侧边栏片段（可复用，放在 .app-shell 内部第一位）
     用法：页面在 include 之前 <c:set var="activeNav" value="dashboard"/>
     分组：业务 / 管理（管理组仅管理员可见，待办项带数量红点）
     注意：本片段只做导航展示，不做任何权限拦截与跳转。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty ctx}">
    <c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
</c:if>
<c:if test="${empty sideUser}">
    <c:set var="sideUser" value="${sessionScope.user}" scope="page"/>
</c:if>
<%-- 导航激活态与角标数量：统一用 c:choose 兜底，避免 Tomcat 11 EL 对 empty 三元表达式的解析问题 --%>
<c:set var="nav" value="" scope="page"/>
<c:if test="${not empty activeNav}">
    <c:set var="nav" value="${activeNav}" scope="page"/>
</c:if>
<c:set var="isEquipment" value="false" scope="page"/>
<c:if test="${nav eq 'equipment' or nav eq 'equipment-detail'}">
    <c:set var="isEquipment" value="true" scope="page"/>
</c:if>
<c:set var="isBorrow" value="false" scope="page"/>
<c:if test="${nav eq 'borrow-my' or nav eq 'borrow-apply'}">
    <c:set var="isBorrow" value="true" scope="page"/>
</c:if>
<c:set var="navPending" value="0" scope="page"/>
<c:if test="${not empty pendingCount}">
    <c:set var="navPending" value="${pendingCount}" scope="page"/>
</c:if>
<c:set var="navOverdue" value="0" scope="page"/>
<c:if test="${not empty overdueCount}">
    <c:set var="navOverdue" value="${overdueCount}" scope="page"/>
</c:if>
<c:set var="navUnread" value="0" scope="page"/>
<c:if test="${not empty sessionScope.unreadNoticeCount}">
    <c:set var="navUnread" value="${sessionScope.unreadNoticeCount}" scope="page"/>
</c:if>

<aside class="side-nav">
    <%-- ============ 品牌 ============ --%>
    <div class="side-brand">
        <span class="side-brand__logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                 stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 3v5a3 3 0 0 0 6 0V3"/>
                <path d="M10.5 3h3"/>
                <path d="M8 21h8"/>
                <path d="M12 21v-6"/>
                <path d="M5.5 8h-3"/>
                <path d="M21.5 8h-3"/>
            </svg>
        </span>
        <span>
            <span class="side-brand__name">实验室设备借用</span>
            <span class="side-brand__sub">昆明学院 · 软件工程</span>
        </span>
    </div>

    <%-- ============ 业务 ============ --%>
    <div class="side-group">
        <div class="side-group__title">业务</div>

        <a class="side-link ${nav eq 'dashboard' ? 'is-active' : ''}" href="${ctx}/dashboard">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                 stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="7" height="7" rx="1.5"/>
                <rect x="14" y="3" width="7" height="7" rx="1.5"/>
                <rect x="3" y="14" width="7" height="7" rx="1.5"/>
                <rect x="14" y="14" width="7" height="7" rx="1.5"/>
            </svg>
            <span>首页仪表盘</span>
        </a>

        <a class="side-link ${isEquipment ? 'is-active' : ''}" href="${ctx}/equipment?action=list">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                 stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                <path d="M3.27 6.96 12 12.01l8.73-5.05M12 22.08V12"/>
            </svg>
            <span>设备一览</span>
        </a>

        <a class="side-link ${isBorrow ? 'is-active' : ''}" href="${ctx}/borrow?action=myRecords">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                 stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 11l3 3L22 4"/>
                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
            </svg>
            <span>我的借用</span>
        </a>

        <a class="side-link ${nav eq 'repair-my' or nav eq 'repair-add' ? 'is-active' : ''}"
           href="${ctx}/repair?action=myRepairs">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                 stroke-linecap="round" stroke-linejoin="round">
                <path d="M14.7 6.3a4 4 0 0 0 5 5l-9.4 9.4a2.1 2.1 0 0 1-3-3z"/>
                <path d="M6.6 6.6 4 4"/>
            </svg>
            <span>我的报修</span>
        </a>

        <a class="side-link ${nav eq 'notice' ? 'is-active' : ''}" href="${ctx}/notice?action=list">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                 stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 8A6 6 0 1 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span>提醒中心</span>
            <c:if test="${navUnread > 0}">
                <span class="badge-dot">${navUnread > 99 ? '99+' : navUnread}</span>
            </c:if>
        </a>
    </div>

    <%-- ============ 管理（仅管理员） ============ --%>
    <c:if test="${sideUser.role == 1}">
        <div class="side-group">
            <div class="side-group__title">管理</div>

            <a class="side-link ${nav eq 'borrow-pending' ? 'is-active' : ''}"
               href="${ctx}/borrow?action=pending">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                     stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="9"/>
                    <path d="M12 7v5l3 2"/>
                </svg>
                <span>待审批</span>
                <c:if test="${navPending > 0}">
                    <span class="badge-dot">${navPending > 99 ? '99+' : navPending}</span>
                </c:if>
            </a>

            <a class="side-link ${nav eq 'borrow-all' ? 'is-active' : ''}"
               href="${ctx}/borrow?action=allRecords">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                     stroke-linecap="round" stroke-linejoin="round">
                    <path d="M8 6h13M8 12h13M8 18h13"/>
                    <path d="M3 6h.01M3 12h.01M3 18h.01"/>
                </svg>
                <span>全部借用</span>
            </a>

            <a class="side-link ${nav eq 'borrow-overdue' ? 'is-active' : ''}"
               href="${ctx}/borrow?action=overdue">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                     stroke-linecap="round" stroke-linejoin="round">
                    <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                    <path d="M12 9v4M12 17h.01"/>
                </svg>
                <span>逾期与提醒</span>
                <c:if test="${navOverdue > 0}">
                    <span class="badge-dot">${navOverdue > 99 ? '99+' : navOverdue}</span>
                </c:if>
            </a>

            <a class="side-link ${nav eq 'repair-all' ? 'is-active' : ''}"
               href="${ctx}/repair?action=allRepairs">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                     stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14.7 6.3a4 4 0 0 1 5 5l-9.4 9.4a2.1 2.1 0 0 1-3-3z"/>
                    <path d="M17.4 8.4 15.6 6.6"/>
                </svg>
                <span>报修处理</span>
            </a>

            <a class="side-link ${nav eq 'admin-user' ? 'is-active' : ''}" href="${ctx}/admin/user">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                     stroke-linecap="round" stroke-linejoin="round">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M22 21v-2a4 4 0 0 0-3-3.87"/>
                </svg>
                <span>用户管理</span>
            </a>
        </div>
    </c:if>

    <div class="side-foot">
        昆明学院 · 软件工程<br>
        实验室设备借用管理系统 v2.0
    </div>
</aside>
