<%-- ============================================================
     逾期与提醒（管理员）：逾期清单 + 一键催还（POST /borrow?action=remind）
     数据：recordList、pageNum、pageTotal、total、keyword
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
<c:set var="activeNav" value="borrow-overdue" scope="page"/>
<c:set var="qs" value="&keyword=${fn:escapeXml(keyword)}" scope="page"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>逾期与提醒 · 实验室设备借用管理系统</title>
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
                    <h1>逾期与提醒</h1>
                    <p class="desc">系统每 30 分钟自动扫描一次逾期记录，也可以在这里手动催还。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/borrow?action=allRecords" class="btn btn--ghost">借用台账</a>
                </div>
            </div>

            <c:if test="${not empty msg}">
                <div class="alert alert--ok">${fn:escapeXml(msg)}</div>
            </c:if>
            <c:if test="${not empty errMsg}">
                <div class="alert alert--danger">${fn:escapeXml(errMsg)}</div>
            </c:if>

            <c:if test="${total gt 0}">
                <div class="alert-bar">
                    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"><path d="M12 8v5M12 17h.01"/><circle cx="12" cy="12" r="9"/></svg>
                    当前有 <strong>${total}</strong> 条借用记录已逾期，建议尽快联系借用人归还
                </div>
            </c:if>

            <section class="panel">
                <form class="toolbar" method="get" action="${ctx}/borrow">
                    <input type="hidden" name="action" value="overdue">
                    <label class="search">
                        <span class="search__icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>
                        </span>
                        <input type="text" name="keyword" class="form-control" placeholder="搜索借用人或设备" value="${fn:escapeXml(keyword)}">
                    </label>
                    <button type="submit" class="btn btn--primary">搜索</button>
                    <a href="${ctx}/borrow?action=overdue" class="btn btn--ghost">重置</a>
                    <span class="toolbar__spacer"></span>
                    <span class="tag tag--overdue">逾期 ${total} 单</span>
                </form>

                <c:choose>
                    <c:when test="${empty recordList}">
                        <div class="panel__body">
                            <div class="empty">
                                <div class="empty__art">
                                    <svg width="84" height="84" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.1" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                                </div>
                                <div class="empty__title">暂无逾期记录</div>
                                <div class="empty__desc">所有设备都在规定时间内归还，状态良好</div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="panel__body">
                            <div class="record-list">
                                <c:forEach items="${recordList}" var="rec">
                                    <div class="record-item record-item--overdue">
                                        <div>
                                            <div class="record-item__title">
                                                ${fn:escapeXml(rec.userName)}
                                                <span class="muted f-13">借用的</span>
                                                ${fn:escapeXml(rec.equipmentName)}
                                                <span class="tag tag--overdue" style="margin-left:6px">逾期 ${rec.overdueDays} 天</span>
                                            </div>
                                            <div class="record-item__sub">
                                                数量 ${rec.quantity} 件 ·
                                                应归还 <fmt:formatDate value="${rec.expectReturnTime}" pattern="yyyy-MM-dd"/>
                                                <c:if test="${not empty rec.userPhone}"> · 联系电话 ${fn:escapeXml(rec.userPhone)}</c:if>
                                            </div>
                                            <c:if test="${rec.remindCount gt 0}">
                                                <div class="record-item__sub">
                                                    已催还 ${rec.remindCount} 次
                                                    <c:if test="${not empty rec.lastRemindTime}">
                                                        ，最近一次 <fmt:formatDate value="${rec.lastRemindTime}" pattern="yyyy-MM-dd HH:mm"/>
                                                    </c:if>
                                                </div>
                                            </c:if>
                                        </div>
                                        <div class="record-item__right">
                                            <form action="${ctx}/borrow?action=remind" method="post"
                                                  onsubmit="return confirm('确认向该同学发送催还提醒？')">
                                                <input type="hidden" name="id" value="${rec.id}">
                                                <button type="submit" class="btn btn--warn btn--sm">催还提醒</button>
                                            </form>
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
                                    <a class="pager__item" href="${ctx}/borrow?action=overdue&pageNum=${pageNum - 1}${qs}">上一页</a>
                                </c:when>
                                <c:otherwise><span class="pager__item is-disabled">上一页</span></c:otherwise>
                            </c:choose>
                            <c:forEach begin="1" end="${pageTotal}" var="i">
                                <c:choose>
                                    <c:when test="${i == pageNum}"><span class="pager__item is-active">${i}</span></c:when>
                                    <c:otherwise><a class="pager__item" href="${ctx}/borrow?action=overdue&pageNum=${i}${qs}">${i}</a></c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <c:choose>
                                <c:when test="${pageNum lt pageTotal}">
                                    <a class="pager__item" href="${ctx}/borrow?action=overdue&pageNum=${pageNum + 1}${qs}">下一页</a>
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
