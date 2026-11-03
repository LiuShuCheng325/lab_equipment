<%-- ============================================================
     提醒中心：系统发给当前用户的全部提醒（审批结果 / 即将到期 / 逾期）
     数据：noticeList、unreadCount、pageNum、pageTotal、total
     注意：循环变量禁止用 eq / ne / lt / gt 等 EL 关键字
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty sessionScope.user}">
    <c:redirect url="/login.jsp"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<c:set var="user" value="${sessionScope.user}" scope="page"/>
<c:set var="activeNav" value="notice" scope="page"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>提醒中心 · 实验室设备借用管理系统</title>
    <%@ include file="/common/css.jsp" %>
</head>
<body>
<div class="app-shell">
    <%@ include file="/common/sidebar.jsp" %>
    <div class="main">
        <%@ include file="/common/navbar.jsp" %>

        <main class="page">
            <div class="page-head">
                <div>
                    <h1>提醒中心</h1>
                    <p class="desc">审批结果、借用到期与逾期催还都会在这里通知你。</p>
                </div>
                <div class="actions">
                    <c:if test="${unreadCount gt 0}">
                        <a href="${ctx}/notice?action=readAll" class="btn btn--ghost">全部标为已读</a>
                    </c:if>
                    <a href="${ctx}/borrow?action=myRecords" class="btn btn--primary">我的借用</a>
                </div>
            </div>

            <c:if test="${not empty msg}">
                <div class="alert alert--ok">${fn:escapeXml(msg)}</div>
            </c:if>

            <section class="panel">
                <div class="panel__head">
                    <h2>全部提醒</h2>
                    <span class="sub">
                        共 ${total} 条
                        <c:if test="${unreadCount gt 0}">，其中 ${unreadCount} 条未读</c:if>
                    </span>
                </div>

                <c:choose>
                    <c:when test="${empty noticeList}">
                        <div class="panel__body">
                            <div class="empty">
                                <div class="empty__art">
                                    <svg width="84" height="84" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.1" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8a6 6 0 1 0-12 0c0 7-3 8-3 8h18s-3-1-3-8"/><path d="M10.3 21a2 2 0 0 0 3.4 0"/></svg>
                                </div>
                                <div class="empty__title">暂无提醒</div>
                                <div class="empty__desc">这里会显示审批结果和逾期催还通知</div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="panel__body">
                            <div class="record-list">
                                <c:forEach items="${noticeList}" var="notice">
                                    <div class="record-item">
                                        <div>
                                            <div class="record-item__title">
                                                <c:if test="${notice.isRead == 0}">
                                                    <span class="breath-dot"></span>
                                                </c:if>
                                                ${fn:escapeXml(notice.title)}
                                            </div>
                                            <div class="record-item__sub">${fn:escapeXml(notice.content)}</div>
                                            <div class="record-item__sub muted">
                                                <fmt:formatDate value="${notice.createTime}" pattern="yyyy-MM-dd HH:mm"/>
                                            </div>
                                        </div>
                                        <div class="record-item__right">
                                            <c:choose>
                                                <c:when test="${notice.type == 1}"><span class="tag tag--warn">即将到期</span></c:when>
                                                <c:when test="${notice.type == 2}"><span class="tag tag--overdue">逾期提醒</span></c:when>
                                                <c:when test="${notice.type == 3}"><span class="tag tag--approved">审批结果</span></c:when>
                                                <c:otherwise><span class="tag tag--plain">系统通知</span></c:otherwise>
                                            </c:choose>
                                            <c:if test="${notice.isRead == 0}">
                                                <a href="${ctx}/notice?action=read&id=${notice.id}" class="btn btn--ghost btn--sm">标为已读</a>
                                            </c:if>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>

                <c:if test="${pageTotal gt 1}">
                    <div class="pager">
                        <span class="info">第 ${pageNum} / ${pageTotal} 页，共 ${total} 条</span>
                        <span class="pager__list">
                            <c:choose>
                                <c:when test="${pageNum gt 1}">
                                    <a class="pager__item" href="${ctx}/notice?action=list&pageNum=${pageNum - 1}">上一页</a>
                                </c:when>
                                <c:otherwise><span class="pager__item is-disabled">上一页</span></c:otherwise>
                            </c:choose>
                            <c:forEach begin="1" end="${pageTotal}" var="i">
                                <c:choose>
                                    <c:when test="${i == pageNum}"><span class="pager__item is-active">${i}</span></c:when>
                                    <c:otherwise><a class="pager__item" href="${ctx}/notice?action=list&pageNum=${i}">${i}</a></c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <c:choose>
                                <c:when test="${pageNum lt pageTotal}">
                                    <a class="pager__item" href="${ctx}/notice?action=list&pageNum=${pageNum + 1}">下一页</a>
                                </c:when>
                                <c:otherwise><span class="pager__item is-disabled">下一页</span></c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </c:if>
            </section>
        </main>
        <%@ include file="/common/footer.jsp" %>
    </div>
</div>
</body>
</html>
