<%-- ============================================================
     借用台账（管理员）：状态概览 + 全量表格 + 筛选 + 导出
     数据：recordList、stat、keyword、status、pageNum、pageTotal、total
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
<c:set var="activeNav" value="borrow-all" scope="page"/>
<c:set var="qs" value="&keyword=${fn:escapeXml(keyword)}&status=${status}" scope="page"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>借用台账 · 实验室设备借用管理系统</title>
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
                    <h1>借用台账</h1>
                    <p class="desc">全部借用记录，可按状态与关键字筛选，支持导出 CSV 存档。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/export?type=borrow" class="btn btn--ghost">导出 CSV</a>
                    <a href="${ctx}/borrow?action=overdue" class="btn btn--primary">逾期与提醒</a>
                </div>
            </div>

            <c:if test="${not empty msg}">
                <div class="alert alert--ok">${fn:escapeXml(msg)}</div>
            </c:if>
            <c:if test="${not empty errMsg}">
                <div class="alert alert--danger">${fn:escapeXml(errMsg)}</div>
            </c:if>

            <div class="stat-grid">
                <div class="stat">
                    <div class="stat__label">总记录</div>
                    <div class="stat__value">${stat.total}<span class="stat__unit">单</span></div>
                </div>
                <div class="stat stat--warn">
                    <div class="stat__label">待审批</div>
                    <div class="stat__value">${stat.pending}<span class="stat__unit">单</span></div>
                </div>
                <div class="stat">
                    <div class="stat__label">借用中</div>
                    <div class="stat__value">${stat.borrowing}<span class="stat__unit">单</span></div>
                </div>
                <div class="stat stat--danger">
                    <div class="stat__label">已逾期</div>
                    <div class="stat__value">${stat.overdue}<span class="stat__unit">单</span></div>
                </div>
                <div class="stat stat--ok">
                    <div class="stat__label">已归还</div>
                    <div class="stat__value">${stat.returned}<span class="stat__unit">单</span></div>
                </div>
            </div>

            <section class="panel" style="margin-top:18px">
                <form class="toolbar" method="get" action="${ctx}/borrow">
                    <input type="hidden" name="action" value="allRecords">
                    <label class="search">
                        <span class="search__icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>
                        </span>
                        <input type="text" name="keyword" class="form-control" placeholder="搜索借用人或设备名" value="${fn:escapeXml(keyword)}">
                    </label>
                    <label class="field" style="margin:0">
                        <select name="status" class="select" id="statusSel">
                            <option value="">全部状态</option>
                            <option value="0">待审批</option>
                            <option value="1">借用中</option>
                            <option value="2">已驳回</option>
                            <option value="3">已归还</option>
                            <option value="4">已逾期</option>
                        </select>
                    </label>
                    <button type="submit" class="btn btn--primary">筛选</button>
                    <a href="${ctx}/borrow?action=allRecords" class="btn btn--ghost">重置</a>
                    <span class="toolbar__spacer"></span>
                    <span class="muted f-13">共 ${total} 条记录</span>
                </form>

                <c:choose>
                    <c:when test="${empty recordList}">
                        <div class="panel__body">
                            <div class="empty">
                                <div class="empty__title">没有符合条件的记录</div>
                                <div class="empty__desc">换个关键字或者清空筛选条件再试试</div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table class="table">
                                <thead>
                                <tr>
                                    <th>借用人</th>
                                    <th>设备</th>
                                    <th class="num">数量</th>
                                    <th>借用日期</th>
                                    <th>预计归还</th>
                                    <th>实际归还</th>
                                    <th>状态</th>
                                    <th>审批人</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${recordList}" var="rec">
                                    <tr>
                                        <td class="strong">${fn:escapeXml(rec.userName)}</td>
                                        <td>${fn:escapeXml(rec.equipmentName)}</td>
                                        <td class="num">${rec.quantity}</td>
                                        <td><fmt:formatDate value="${rec.borrowTime}" pattern="yyyy-MM-dd"/></td>
                                        <td><fmt:formatDate value="${rec.expectReturnTime}" pattern="yyyy-MM-dd"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty rec.actualReturnTime}">—</c:when>
                                                <c:otherwise><fmt:formatDate value="${rec.actualReturnTime}" pattern="yyyy-MM-dd"/></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${rec.status == 0}"><span class="tag tag--pending">待审批</span></c:when>
                                                <c:when test="${rec.status == 1}"><span class="tag tag--approved">借用中</span></c:when>
                                                <c:when test="${rec.status == 2}"><span class="tag tag--rejected">已驳回</span></c:when>
                                                <c:when test="${rec.status == 3}"><span class="tag tag--returned">已归还</span></c:when>
                                                <c:when test="${rec.status == 4}"><span class="tag tag--overdue">已逾期</span></c:when>
                                                <c:otherwise><span class="tag tag--plain">未知</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="muted">
                                            <c:choose>
                                                <c:when test="${empty rec.approver}">—</c:when>
                                                <c:otherwise>${fn:escapeXml(rec.approver)}</c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>

                <c:if test="${pageTotal gt 1}">
                    <div class="pager">
                        <span class="info">第 ${pageNum} / ${pageTotal} 页，共 ${total} 条</span>
                        <span class="pager__list">
                            <c:choose>
                                <c:when test="${pageNum gt 1}">
                                    <a class="pager__item" href="${ctx}/borrow?action=allRecords&pageNum=${pageNum - 1}${qs}">上一页</a>
                                </c:when>
                                <c:otherwise><span class="pager__item is-disabled">上一页</span></c:otherwise>
                            </c:choose>
                            <c:forEach begin="1" end="${pageTotal}" var="i">
                                <c:choose>
                                    <c:when test="${i == pageNum}"><span class="pager__item is-active">${i}</span></c:when>
                                    <c:otherwise><a class="pager__item" href="${ctx}/borrow?action=allRecords&pageNum=${i}${qs}">${i}</a></c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <c:choose>
                                <c:when test="${pageNum lt pageTotal}">
                                    <a class="pager__item" href="${ctx}/borrow?action=allRecords&pageNum=${pageNum + 1}${qs}">下一页</a>
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
<script>
(function () {
    var sel = document.getElementById('statusSel');
    if (!sel) { return; }
    var want = '${status}';
    for (var i = 0; i < sel.options.length; i++) {
        if (sel.options[i].value === want) { sel.selectedIndex = i; }
    }
})();
</script>
</body>
</html>
