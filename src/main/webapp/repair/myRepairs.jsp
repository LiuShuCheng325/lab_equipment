<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty sessionScope.user}">
    <c:redirect url="/login.jsp"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<c:set var="user" value="${sessionScope.user}" scope="page"/>
<c:set var="activeNav" value="repair-my" scope="page"/>
<c:set var="pageTitle" value="我的报修" scope="page"/>

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
                    <p class="desc">共 <strong>${fn:length(recordList)}</strong> 条报修记录，可查看管理员的处理意见。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/equipment?action=list" class="btn btn--primary">去设备一览报修</a>
                </div>
            </div>

            <section class="panel">
                <div class="panel__head">
                    <h2>报修记录</h2>
                    <span class="sub">共 ${empty total ? fn:length(recordList) : total} 条</span>
                </div>

                <div class="panel__body">
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
                                <div class="empty__title">还没有提交过报修</div>
                                <div class="empty__desc">设备出现问题时，可在设备详情页提交报修申请</div>
                                <a href="${ctx}/equipment?action=list" class="btn btn--primary empty__action">去设备一览</a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="record-list">
                                <c:forEach items="${recordList}" var="r">
                                    <div class="record-item ${r.status == 0 ? 'record-item--pending' : ''}">
                                        <div class="record-item__main">
                                            <div class="record-item__title">
                                                ${fn:escapeXml(r.equipmentName)}
                                            </div>
                                            <div class="borrow-card__meta">
                                                ${fn:escapeXml(r.description)}
                                            </div>
                                            <div class="input-tip">
                                                提交时间：<fmt:formatDate value="${r.applyTime}" pattern="yyyy-MM-dd HH:mm"/>
                                                <c:if test="${not empty r.completeTime}">
                                                    · 完成时间：<fmt:formatDate value="${r.completeTime}" pattern="yyyy-MM-dd HH:mm"/>
                                                </c:if>
                                            </div>
                                        </div>

                                        <div class="record-item__right">
                                            <c:choose>
                                                <c:when test="${r.status == 0}">
                                                    <span class="tag tag--pending">待处理</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="tag tag--returned">已处理</span>
                                                </c:otherwise>
                                            </c:choose>
                                            <c:if test="${not empty r.handler}">
                                                <span class="muted f-12">处理人：${fn:escapeXml(r.handler)}</span>
                                            </c:if>
                                        </div>
                                    </div>

                                    <c:if test="${not empty r.remark}">
                                        <div class="overdue-banner overdue-banner--info">
                                            处理意见：${fn:escapeXml(r.remark)}
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>
        </div>

        <%@ include file="/common/footer.jsp" %>
    </div>
</div>

<script src="${ctx}/js/app.js?v=4.3"></script>
</body>
</html>
