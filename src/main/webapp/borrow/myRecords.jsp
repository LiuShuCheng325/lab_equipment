<%-- ============================================================
     我的借用（卡片列表 + 逾期红边框 + POST 归还）
     数据：recordList、myPendingCount、myBorrowingCount、myReturnedCount、myOverdueCount、
           pageNum、pageTotal、total、keyword、status
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
<c:set var="activeNav" value="borrow-my" scope="page"/>
<c:set var="qs" value="&keyword=${fn:escapeXml(keyword)}&status=${status}" scope="page"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>我的借用 · 实验室设备借用管理系统</title>
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
                    <h1>我的借用</h1>
                    <p class="desc">查看你提交的全部借用申请，借用中的设备可在到期后一键归还。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/equipment?action=list" class="btn btn--primary">去借设备</a>
                </div>
            </div>

            <c:if test="${not empty msg}">
                <div class="alert alert--ok">${fn:escapeXml(msg)}</div>
            </c:if>
            <c:if test="${not empty errMsg}">
                <div class="alert alert--danger">${fn:escapeXml(errMsg)}</div>
            </c:if>

            <%-- 四张小统计卡 --%>
            <div class="stat-grid">
                <div class="stat">
                    <div class="stat__label">待审批</div>
                    <div class="stat__value">${myPendingCount}<span class="stat__unit">单</span></div>
                    <div class="stat__foot">提交后等待管理员审核</div>
                </div>
                <div class="stat">
                    <div class="stat__label">借用中</div>
                    <div class="stat__value">${myBorrowingCount}<span class="stat__unit">单</span></div>
                    <div class="stat__foot">到期前请按时归还</div>
                </div>
                <div class="stat stat--danger">
                    <div class="stat__label">
                        已逾期
                        <c:if test="${myOverdueCount gt 0}"><span class="breath-dot"></span></c:if>
                    </div>
                    <div class="stat__value">${myOverdueCount}<span class="stat__unit">单</span></div>
                    <div class="stat__foot">
                        <c:choose>
                            <c:when test="${myOverdueCount gt 0}">请尽快归还处理</c:when>
                            <c:otherwise>暂无逾期，保持良好信用</c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat__label">已归还</div>
                    <div class="stat__value">${myReturnedCount}<span class="stat__unit">单</span></div>
                    <div class="stat__foot">历史归还记录</div>
                </div>
            </div>

            <section class="panel" style="margin-top:18px">
                <form class="toolbar" method="get" action="${ctx}/borrow">
                    <input type="hidden" name="action" value="myRecords">
                    <label class="search">
                        <span class="search__icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>
                        </span>
                        <input type="text" name="keyword" class="form-control" placeholder="搜索设备名称" value="${fn:escapeXml(keyword)}">
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
                    <a href="${ctx}/borrow?action=myRecords" class="btn btn--ghost">重置</a>
                    <span class="toolbar__spacer"></span>
                    <span class="muted f-13">共 ${total} 条记录</span>
                </form>

                <c:choose>
                    <c:when test="${empty recordList}">
                        <div class="panel__body">
                            <div class="empty">
                                <div class="empty__art">
                                    <svg width="84" height="84" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.1" stroke-linecap="round" stroke-linejoin="round"><rect x="3.5" y="5" width="17" height="15" rx="2"/><path d="M8 3v4M16 3v4M3.5 10h17"/></svg>
                                </div>
                                <div class="empty__title">还没有借用记录</div>
                                <div class="empty__desc">去设备列表挑一台设备，提交你的第一份借用申请</div>
                                <a href="${ctx}/equipment?action=list" class="btn btn--primary empty__action">浏览设备</a>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="panel__body">
                            <div class="record-list">
                                <c:forEach items="${recordList}" var="rec">
                                    <c:set var="isOverdue" value="${rec.status == 4}" scope="page"/>
                                    <div class="record-item">
                                        <div>
                                            <div class="record-item__title">${fn:escapeXml(rec.equipmentName)}</div>
                                            <div class="record-item__sub">
                                                借用 ${rec.quantity} 件 ·
                                                <fmt:formatDate value="${rec.borrowTime}" pattern="yyyy-MM-dd"/>
                                                至 <fmt:formatDate value="${rec.expectReturnTime}" pattern="yyyy-MM-dd"/>
                                            </div>
                                            <c:if test="${not empty rec.remark}">
                                                <div class="record-item__sub">用途：${fn:escapeXml(rec.remark)}</div>
                                            </c:if>
                                            <c:if test="${not empty rec.auditRemark}">
                                                <div class="record-item__sub">审批意见：${fn:escapeXml(rec.auditRemark)}</div>
                                            </c:if>
                                            <c:if test="${isOverdue}">
                                                <div class="overdue-banner">
                                                    已逾期 ${rec.overdueDays} 天，请尽快归还，逾期记录将影响后续借用
                                                </div>
                                            </c:if>
                                        </div>

                                        <div class="record-item__right">
                                            <c:choose>
                                                <c:when test="${rec.status == 0}"><span class="tag tag--pending">待审批</span></c:when>
                                                <c:when test="${rec.status == 1}"><span class="tag tag--approved">借用中</span></c:when>
                                                <c:when test="${rec.status == 2}"><span class="tag tag--rejected">已驳回</span></c:when>
                                                <c:when test="${rec.status == 3}"><span class="tag tag--returned">已归还</span></c:when>
                                                <c:when test="${rec.status == 4}"><span class="tag tag--overdue">已逾期</span></c:when>
                                                <c:otherwise><span class="tag tag--plain">未知</span></c:otherwise>
                                            </c:choose>

                                            <c:if test="${rec.status == 1 or rec.status == 4}">
                                                <form action="${ctx}/borrow?action=return" method="post"
                                                      onsubmit="return confirm('确认已归还该设备吗？归还后库存将自动恢复。')">
                                                    <input type="hidden" name="id" value="${rec.id}">
                                                    <input type="hidden" name="returnCondition" value="0">
                                                    <button type="submit" class="btn btn--primary btn--sm">归还</button>
                                                </form>
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
                                    <a class="pager__item" href="${ctx}/borrow?action=myRecords&pageNum=${pageNum - 1}${qs}">上一页</a>
                                </c:when>
                                <c:otherwise><span class="pager__item is-disabled">上一页</span></c:otherwise>
                            </c:choose>
                            <c:forEach begin="1" end="${pageTotal}" var="i">
                                <c:choose>
                                    <c:when test="${i == pageNum}"><span class="pager__item is-active">${i}</span></c:when>
                                    <c:otherwise><a class="pager__item" href="${ctx}/borrow?action=myRecords&pageNum=${i}${qs}">${i}</a></c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <c:choose>
                                <c:when test="${pageNum lt pageTotal}">
                                    <a class="pager__item" href="${ctx}/borrow?action=myRecords&pageNum=${pageNum + 1}${qs}">下一页</a>
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
