<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- 权限校验统一由 RepairServlet 处理，本页面只做展示 --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<c:set var="user" value="${sessionScope.user}" scope="page"/>
<c:set var="activeNav" value="repair-all" scope="page"/>
<c:set var="pageTitle" value="报修处理" scope="page"/>

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
                    <p class="desc">处理实验室设备报修申请，处理后请填写处理意见，便于报修人了解进展。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/repair?action=myRepairs" class="btn btn--ghost">我的报修</a>
                    <a href="${ctx}/equipment?action=list" class="btn btn--primary">设备一览</a>
                </div>
            </div>

            <section class="panel">
                <div class="panel__head">
                    <h2>报修台账</h2>
                    <span class="sub">共 ${empty total ? fn:length(recordList) : total} 条记录</span>
                </div>

                <div class="panel__body panel__body--flush">
                    <c:choose>
                        <c:when test="${empty recordList}">
                            <div class="empty">
                                <div class="empty__art">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"
                                         stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M14.7 6.3a4 4 0 0 1 5 5l-9.4 9.4a2.1 2.1 0 0 1-3-3z"/>
                                        <path d="M6.6 6.6 4 4"/>
                                    </svg>
                                </div>
                                <div class="empty__title">暂无报修记录</div>
                                <div class="empty__desc">所有设备运行正常，暂无待处理的报修申请</div>
                                <a href="${ctx}/equipment?action=list" class="btn btn--primary empty__action">去设备一览</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="table">
                                    <thead>
                                    <tr>
                                        <th>报修人</th>
                                        <th>设备</th>
                                        <th>故障描述</th>
                                        <th class="nowrap">提交时间</th>
                                        <th style="width:100px">状态</th>
                                        <th>处理人</th>
                                        <th>处理意见</th>
                                        <th class="text-right" style="width:130px">操作</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${recordList}" var="rec">
                                        <tr>
                                            <td class="strong">${fn:escapeXml(rec.userName)}</td>
                                            <td>${fn:escapeXml(rec.equipmentName)}</td>
                                            <td class="muted">${fn:escapeXml(rec.description)}</td>
                                            <td class="nowrap">
                                                <fmt:formatDate value="${rec.applyTime}" pattern="yyyy-MM-dd HH:mm"/>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${rec.status == 0}">
                                                        <span class="tag tag--pending">待处理</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="tag tag--returned">已处理</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="muted">
                                                <c:choose>
                                                    <c:when test="${empty rec.handler}">—</c:when>
                                                    <c:otherwise>${fn:escapeXml(rec.handler)}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="muted f-12">
                                                <c:choose>
                                                    <c:when test="${empty rec.remark}">—</c:when>
                                                    <c:otherwise>${fn:escapeXml(rec.remark)}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-right">
                                                <div class="actions" style="justify-content:flex-end">
                                                    <c:choose>
                                                        <c:when test="${rec.status == 0}">
                                                            <a href="${ctx}/repair?action=handle&id=${rec.id}&status=1"
                                                               class="btn btn--ok btn--sm">标记已处理</a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="muted f-12">已完成</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
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
