<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- EL变量类型声明，消除IDEA警告 --%>
<%--@elvariable id="userList" type="java.util.List<com.lab.entity.User>"--%>
<%-- 权限校验统一由 UserManageServlet 处理，本页面只做展示 --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<c:set var="user" value="${sessionScope.user}" scope="page"/>
<c:set var="activeNav" value="admin-user" scope="page"/>
<c:set var="pageTitle" value="用户管理" scope="page"/>

<%-- 角色分布统计（基于当前查询结果的补集推算，不额外请求后端） --%>
<c:set var="totalUser" value="${empty total ? 0 : total}" scope="page"/>
<c:set var="adminCount" value="0" scope="page"/>
<c:forEach items="${userList}" var="row">
    <c:if test="${row.role == 1}">
        <c:set var="adminCount" value="${adminCount + 1}" scope="page"/>
    </c:if>
</c:forEach>
<c:set var="studentCount" value="${totalUser - adminCount}" scope="page"/>

<%-- 分页基础参数（保留搜索条件） --%>
<c:set var="baseUrl" value="${ctx}/admin/user" scope="page"/>
<c:set var="qs" value="&keyword=${fn:escapeXml(keyword)}" scope="page"/>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${fn:escapeXml(pageTitle)} - 实验室设备借用管理系统</title>
    <%@ include file="/common/css.jsp" %>
</head>
<body>

<div class="app-shell">
    <%@ include file="/common/sidebar.jsp" %>

    <div class="main">
        <%@ include file="/common/navbar.jsp" %>

        <div class="page">
            <div class="page-head">
                <div>
                    <h1>${fn:escapeXml(pageTitle)}</h1>
                    <p class="desc">查看与维护系统账号，可修改资料、调整角色，或清理失效账号。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/borrow?action=allRecords" class="btn btn--ghost">借用台账</a>
                </div>
            </div>

            <%-- ============ 概览 ============ --%>
            <div class="stat-grid">
                <div class="stat">
                    <div class="stat__label">账号总数</div>
                    <div class="stat__value">${totalUser}<span class="stat__unit">个</span></div>
                    <div class="stat__foot">符合当前筛选条件</div>
                </div>
                <div class="stat">
                    <div class="stat__label">管理员</div>
                    <div class="stat__value">${adminCount}<span class="stat__unit">个</span></div>
                    <div class="stat__foot">本页可见的管理员数</div>
                </div>
                <div class="stat stat--ok">
                    <div class="stat__label">借用用户</div>
                    <div class="stat__value">${studentCount}<span class="stat__unit">个</span></div>
                    <div class="stat__foot">总数减去管理员数</div>
                </div>
                <div class="stat">
                    <div class="stat__label">当前登录</div>
                    <div class="stat__value">${fn:escapeXml(user.name)}</div>
                    <div class="stat__foot">${user.role == 1 ? '管理员身份' : '普通用户身份'}</div>
                </div>
            </div>

            <%-- ============ 账号列表 ============ --%>
            <section class="panel">
                <div class="panel__head">
                    <h2>账号列表</h2>
                    <span class="sub">共 ${totalUser} 个账号</span>
                </div>

                <form class="toolbar" method="get" action="${ctx}/admin/user">
                    <input type="hidden" name="action" value="list">
                    <label class="search">
                        <span class="search__icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                                 stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="11" cy="11" r="7"/>
                                <path d="m20 20-3.5-3.5"/>
                            </svg>
                        </span>
                        <input type="text" name="keyword" class="form-control"
                               value="${fn:escapeXml(keyword)}" placeholder="搜索用户名 / 姓名 / 手机号 / 邮箱">
                    </label>
                    <button type="submit" class="btn btn--primary">搜索</button>
                    <a href="${ctx}/admin/user" class="btn btn--ghost">重置</a>
                </form>

                <div class="panel__body panel__body--flush">
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                            <tr>
                                <th style="width:64px">ID</th>
                                <th>账号</th>
                                <th>姓名</th>
                                <th>电话</th>
                                <th>邮箱</th>
                                <th style="width:110px">角色</th>
                                <th class="text-right" style="width:170px">操作</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:choose>
                                <c:when test="${empty userList}">
                                    <tr>
                                        <td colspan="7">
                                            <div class="empty">
                                                <div class="empty__art">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                                         stroke-width="1.2" stroke-linecap="round"
                                                         stroke-linejoin="round">
                                                        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                                                        <circle cx="9" cy="7" r="4"/>
                                                        <path d="M22 21v-2a4 4 0 0 0-3-3.87"/>
                                                    </svg>
                                                </div>
                                                <div class="empty__title">
                                                    <c:choose>
                                                        <c:when test="${not empty keyword}">没有匹配的账号</c:when>
                                                        <c:otherwise>暂无系统账号</c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div class="empty__desc">
                                                    <c:choose>
                                                        <c:when test="${not empty keyword}">试试更换关键字，或清空搜索条件</c:when>
                                                        <c:otherwise>用户在注册页面完成注册后会出现在这里</c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <c:if test="${not empty keyword}">
                                                    <a href="${ctx}/admin/user" class="btn btn--ghost empty__action">清空搜索</a>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${userList}" var="row">
                                        <tr>
                                            <td class="muted">#${row.id}</td>
                                            <td class="strong">${fn:escapeXml(row.username)}</td>
                                            <td>${fn:escapeXml(row.name)}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${empty row.phone}"><span class="muted">未填写</span></c:when>
                                                    <c:otherwise>${fn:escapeXml(row.phone)}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${empty row.email}"><span class="muted">未填写</span></c:when>
                                                    <c:otherwise>${fn:escapeXml(row.email)}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${row.role == 1}">
                                                        <span class="tag tag--approved">管理员</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="tag tag--plain">借用用户</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-right">
                                                <div class="actions" style="justify-content:flex-end">
                                                    <a href="${ctx}/admin/user?action=edit&id=${row.id}"
                                                       class="btn btn--default btn--sm">编辑</a>
                                                    <c:choose>
                                                        <c:when test="${row.id == user.id}">
                                                            <span class="muted f-12">当前账号</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form method="post" action="${ctx}/admin/user"
                                                              onsubmit="return confirm('确定要删除账号「${fn:escapeXml(row.username)}」吗？删除后该账号将无法登录。')">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="id" value="${row.id}">
                                                            <button type="submit"
                                                                    class="btn btn--danger btn--sm">删除
                                                            </button>
                                                        </form>
                                                    </c:otherwise>
                                                </c:choose>
                                        </div>
                                    </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <%@ include file="/common/pager.jsp" %>

                <div class="panel__foot">
                    <span class="muted f-12">
                        提示：将账号角色调整为「管理员」后，该账号即可进入审批与设备维护功能。
                    </span>
                </div>
            </section>
        </div>

        <%@ include file="/common/footer.jsp" %>
    </div>
</div>

<div class="toast-host" id="toastHost"
     data-msg="${fn:escapeXml(msg)}" data-err="${fn:escapeXml(errMsg)}"></div>
<script src="${ctx}/js/app.js?v=4.3"></script>
</body>
</html>
