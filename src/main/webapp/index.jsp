<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty sessionScope.user}">
    <c:redirect url="/login.jsp"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<c:set var="user" value="${sessionScope.user}" scope="page"/>
<c:set var="activeNav" value="dashboard" scope="page"/>
<c:set var="pageTitle" value="首页仪表盘" scope="page"/>
<c:set var="d" value="${requestScope.dashboard}" scope="page"/>
<jsp:useBean id="today" class="java.util.Date" scope="page"/>

<%-- 计算趋势柱状图峰值与类别总数，避免除零 --%>
<c:set var="trendMax" value="1" scope="page"/>
<c:forEach items="${d.trend}" var="day">
    <c:if test="${day.count > trendMax}">
        <c:set var="trendMax" value="${day.count}" scope="page"/>
    </c:if>
</c:forEach>
<c:set var="catTotal" value="0" scope="page"/>
<c:forEach items="${d.categories}" var="cat">
    <c:set var="catTotal" value="${catTotal + cat.count}" scope="page"/>
</c:forEach>

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
            <%-- ============ 页头 ============ --%>
            <div class="page-head">
                <div>
                    <h1>
                        <c:choose>
                            <c:when test="${user.role == 1}">管理总览</c:when>
                            <c:otherwise>我的工作台</c:otherwise>
                        </c:choose>
                    </h1>
                    <p class="desc">
                        欢迎回来，${fn:escapeXml(user.name)}。
                        <c:choose>
                            <c:when test="${user.role == 1}">这里是实验室设备与借用业务的实时概览。</c:when>
                            <c:otherwise>请按时归还设备，逾期记录会影响后续借用审批。</c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <div class="actions">
                    <a href="${ctx}/equipment?action=list" class="btn btn--primary">浏览设备</a>
                    <c:if test="${user.role == 1}">
                        <a href="${ctx}/borrow?action=pending" class="btn btn--ghost">待审批 ${d.pendingCount}</a>
                    </c:if>
                </div>
            </div>

            <%-- ============ 逾期强提示（有逾期才出现） ============ --%>
            <c:if test="${d.myOverdueCount > 0}">
                <div class="alert-bar">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                         stroke-linecap="round" stroke-linejoin="round">
                        <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                        <path d="M12 9v4M12 17h.01"/>
                    </svg>
                    <span>
                        您有 <strong>${d.myOverdueCount}</strong> 条借用已逾期，请尽快归还，
                        逾期记录会影响后续借用审批。
                        <a href="${ctx}/borrow?action=myRecords">去处理 →</a>
                    </span>
                </div>
            </c:if>

            <%-- ============ 核心统计 ============ --%>
            <div class="stat-grid">
                <div class="stat">
                    <div class="stat__label">设备总数</div>
                    <div class="stat__value">${d.equipmentTotal}<span class="stat__unit">台</span></div>
                    <div class="stat__foot">正常 ${d.equipmentNormal} · 维修中 ${d.equipmentRepairing}</div>
                    <div class="stat__icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4"
                             stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                            <path d="M3.27 6.96 12 12.01l8.73-5.05M12 22.08V12"/>
                        </svg>
                    </div>
                </div>

                <div class="stat stat--ok">
                    <div class="stat__label">当前可借</div>
                    <div class="stat__value">${d.equipmentAvailable}<span class="stat__unit">件</span></div>
                    <div class="stat__foot">可立即提交借用申请</div>
                    <div class="stat__icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4"
                             stroke-linecap="round" stroke-linejoin="round">
                            <path d="M20 6 9 17l-5-5"/>
                        </svg>
                    </div>
                </div>

                <div class="stat">
                    <div class="stat__label">借用中</div>
                    <div class="stat__value">${d.borrowingCount}<span class="stat__unit">单</span></div>
                    <div class="stat__foot">即将到期 ${d.dueSoonCount} 单</div>
                    <div class="stat__icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4"
                             stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="12" cy="12" r="9"/>
                            <path d="M12 7v5l3 2"/>
                        </svg>
                    </div>
                </div>

                <div class="stat ${d.overdueCount > 0 ? 'stat--danger' : ''}">
                    <div class="stat__label">
                        <c:if test="${d.overdueCount > 0}"><span class="breath-dot"></span></c:if>
                        逾期未还
                    </div>
                    <div class="stat__value">${d.overdueCount}<span class="stat__unit">单</span></div>
                    <div class="stat__foot">
                        <c:choose>
                            <c:when test="${d.overdueCount > 0}">需尽快催还处理</c:when>
                            <c:otherwise>全部按时归还</c:otherwise>
                        </c:choose>
                    </div>
                    <div class="stat__icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.4"
                             stroke-linecap="round" stroke-linejoin="round">
                            <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                            <path d="M12 9v4M12 17h.01"/>
                        </svg>
                    </div>
                </div>
            </div>

            <%-- ============ 趋势 + 类别分布 ============ --%>
            <div class="grid-2">
                <section class="panel">
                    <div class="panel__head">
                        <h2>最近 7 天借用申请</h2>
                        <span class="sub">按日统计提交量</span>
                    </div>
                    <div class="panel__body">
                        <c:choose>
                            <c:when test="${empty d.trend}">
                                <div class="empty">
                                    <div class="empty__art">
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"
                                             stroke-linecap="round" stroke-linejoin="round">
                                            <path d="M3 3v18h18"/>
                                            <path d="M7 15v-3M12 15V8M17 15v-6"/>
                                        </svg>
                                    </div>
                                    <div class="empty__title">暂无借用数据</div>
                                    <div class="empty__desc">还没有产生借用申请记录</div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="bar-chart">
                                    <c:forEach items="${d.trend}" var="day">
                                        <c:set var="h" value="${day.count * 100 / trendMax}" scope="page"/>
                                        <div class="bar-chart__col">
                                            <div class="bar-chart__value">${day.count}</div>
                                            <div class="bar-chart__bar"
                                                 style="height:${day.count > 0 && h < 4 ? 4 : h}%"></div>
                                            <div class="bar-chart__label">${fn:escapeXml(fn:substring(day.date, 5, 10))}</div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>

                <section class="panel">
                    <div class="panel__head">
                        <h2>设备类别分布</h2>
                        <span class="sub">共 ${catTotal} 台</span>
                    </div>
                    <div class="panel__body">
                        <c:choose>
                            <c:when test="${empty d.categories}">
                                <div class="empty">
                                    <div class="empty__title">暂无设备数据</div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="hbar-list">
                                <c:forEach items="${d.categories}" var="cat">
                                    <c:set var="pct" value="${catTotal > 0 ? cat.count * 100 / catTotal : 0}" scope="page"/>
                                    <div>
                                        <div class="hbar__top">
                                            <span class="hbar__name">${fn:escapeXml(cat.name)}</span>
                                            <span class="hbar__value">${cat.count} 台 ·
                                                <fmt:formatNumber value="${pct}" maxFractionDigits="0"/>%</span>
                                        </div>
                                        <div class="hbar__track">
                                            <div class="hbar__fill" style="width:${pct < 3 ? 3 : pct}%"></div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>
        </div>

        <%-- ============ 待办 / 我的借阅 ============ --%>
        <c:choose>
            <c:when test="${user.role == 1}">
                <section class="panel">
                    <div class="panel__head">
                        <h2>待办事项</h2>
                        <span class="sub">点击卡片直接进入处理页</span>
                    </div>
                    <div class="panel__body">
                        <div class="todo-grid">
                            <a class="todo-card ${d.pendingCount > 0 ? 'todo-card--warn' : ''}"
                               href="${ctx}/borrow?action=pending">
                                <div class="todo-card__icon">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                                         stroke-linecap="round" stroke-linejoin="round">
                                        <circle cx="12" cy="12" r="9"/>
                                        <path d="M12 7v5l3 2"/>
                                    </svg>
                                </div>
                                <div>
                                    <div class="todo-card__num">${d.pendingCount}</div>
                                    <div class="todo-card__label">待审批借用</div>
                                </div>
                            </a>
                            <a class="todo-card ${d.overdueCount > 0 ? 'todo-card--danger' : ''}"
                               href="${ctx}/borrow?action=overdue">
                                <div class="todo-card__icon">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                                         stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                                        <path d="M12 9v4M12 17h.01"/>
                                    </svg>
                                </div>
                                <div>
                                    <div class="todo-card__num">${d.overdueCount}</div>
                                    <div class="todo-card__label">逾期未还</div>
                                </div>
                            </a>
                            <a class="todo-card ${d.dueSoonCount > 0 ? 'todo-card--warn' : ''}"
                               href="${ctx}/borrow?action=allRecords&status=1">
                                <div class="todo-card__icon">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                                         stroke-linecap="round" stroke-linejoin="round">
                                        <circle cx="12" cy="12" r="9"/>
                                        <path d="M12 8v5"/>
                                        <circle cx="12" cy="16.5" r="0.9" fill="currentColor" stroke="none"/>
                                    </svg>
                                </div>
                                <div>
                                    <div class="todo-card__num">${d.dueSoonCount}</div>
                                    <div class="todo-card__label">3 天内到期</div>
                                </div>
                            </a>
                            <a class="todo-card" href="${ctx}/repair?action=allRepairs">
                                <div class="todo-card__icon">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                                         stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M14.7 6.3a4 4 0 0 1 5 5l-9.4 9.4a2.1 2.1 0 0 1-3-3z"/>
                                        <path d="M17.4 8.4 15.6 6.6"/>
                                    </svg>
                                </div>
                                <div>
                                    <div class="todo-card__num">${d.pendingRepairCount}</div>
                                    <div class="todo-card__label">待处理报修</div>
                                </div>
                            </a>
                        </div>
                    </div>
                </section>

                <section class="panel">
                    <div class="panel__head">
                        <h2>借用业务概况</h2>
                        <span class="sub">全站累计</span>
                    </div>
                    <div class="panel__body">
                        <div class="todo-grid">
                            <div class="todo-card">
                                <div>
                                    <div class="todo-card__num">${d.borrowingCount}</div>
                                    <div class="todo-card__label">借用中</div>
                                </div>
                            </div>
                            <div class="todo-card">
                                <div>
                                    <div class="todo-card__num">${d.returnedCount}</div>
                                    <div class="todo-card__label">已归还</div>
                                </div>
                            </div>
                            <div class="todo-card">
                                <div>
                                    <div class="todo-card__num">${d.rejectedCount}</div>
                                    <div class="todo-card__label">已驳回</div>
                                </div>
                            </div>
                            <div class="todo-card ${d.overdueCount > 0 ? 'todo-card--danger' : ''}">
                                <div>
                                    <div class="todo-card__num">${d.overdueCount}</div>
                                    <div class="todo-card__label">逾期</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </section>
            </c:when>

            <c:otherwise>
                <section class="panel">
                    <div class="panel__head">
                        <h2>我的借阅概览</h2>
                        <span class="sub">待审批 ${d.myPendingCount} · 借用中 ${d.myBorrowingCount} · 逾期 ${d.myOverdueCount}</span>
                        <div class="right">
                            <a href="${ctx}/borrow?action=myRecords" class="btn btn--ghost btn--sm">查看全部记录</a>
                        </div>
                    </div>
                    <div class="panel__body">
                        <c:choose>
                            <c:when test="${empty myBorrowList}">
                                <div class="empty">
                                    <div class="empty__art">
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2"
                                             stroke-linecap="round" stroke-linejoin="round">
                                            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                                            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                                        </svg>
                                    </div>
                                    <div class="empty__title">当前没有在借设备</div>
                                    <div class="empty__desc">去设备一览挑一台，提交借用申请吧</div>
                                    <a href="${ctx}/equipment?action=list" class="btn btn--primary empty__action">去借设备</a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="borrow-card-grid">
                                    <c:forEach items="${myBorrowList}" var="r">
                                        <%-- 剩余天数：向下取整，负数代表已逾期 --%>
                                        <c:set var="ms" value="${r.expectReturnTime.time - today.time}" scope="page"/>
                                        <c:set var="remain" value="${(ms - ms % 86400000) / 86400000}" scope="page"/>
                                        <div class="borrow-card">
                                            <div class="row-between">
                                                <span class="borrow-card__name">${fn:escapeXml(r.equipmentName)}</span>
                                                <c:choose>
                                                    <c:when test="${remain < 0}">
                                                        <span class="tag tag--overdue">已逾期</span>
                                                    </c:when>
                                                    <c:when test="${remain <= 3}">
                                                        <span class="tag tag--pending">即将到期</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="tag tag--approved">借用中</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="borrow-card__meta">
                                                数量 ${r.quantity} 件　·　借出
                                                <fmt:formatDate value="${r.borrowTime}" pattern="yyyy-MM-dd"/>
                                            </div>
                                            <div class="borrow-card__meta">
                                                应还日期
                                                <fmt:formatDate value="${r.expectReturnTime}" pattern="yyyy-MM-dd"/>
                                            </div>
                                            <div class="mt-8">
                                                <c:choose>
                                                    <c:when test="${remain < 0}">
                                                        <span class="remain remain--danger">已逾期 ${0 - remain} 天，请尽快归还</span>
                                                    </c:when>
                                                    <c:when test="${remain == 0}">
                                                        <span class="remain remain--warn">今天到期，请今日归还</span>
                                                    </c:when>
                                                    <c:when test="${remain <= 3}">
                                                        <span class="remain remain--warn">还剩 ${remain} 天到期</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="remain remain--ok">还剩 ${remain} 天</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>

                <section class="panel">
                    <div class="panel__head">
                        <h2>快捷入口</h2>
                    </div>
                    <div class="panel__body">
                        <div class="actions">
                            <a href="${ctx}/equipment?action=list" class="btn btn--primary">申请借用设备</a>
                            <a href="${ctx}/borrow?action=myRecords" class="btn btn--ghost">查看我的借用</a>
                            <a href="${ctx}/notice?action=list" class="btn btn--ghost">查看提醒</a>
                            <a href="${ctx}/repair?action=myRepairs" class="btn btn--ghost">提交报修</a>
                        </div>
                    </div>
                </section>
            </c:otherwise>
        </c:choose>
        </div>

        <%@ include file="/common/footer.jsp" %>
    </div>
</div>

<div class="toast-host" id="toastHost"
     data-msg="${fn:escapeXml(msg)}" data-err="${fn:escapeXml(errMsg)}"></div>
<script src="${ctx}/js/app.js?v=4.3"></script>
</body>
</html>
