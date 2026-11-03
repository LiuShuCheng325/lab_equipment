<%-- ============================================================
     借用审批（管理员）：待审批卡片 + 通过/驳回（POST）
     数据：recordList、keyword、pageNum、pageTotal、total
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
<c:set var="activeNav" value="borrow-pending" scope="page"/>
<c:set var="qs" value="&keyword=${fn:escapeXml(keyword)}" scope="page"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>借用审批 · 实验室设备借用管理系统</title>
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
                    <h1>借用审批</h1>
                    <p class="desc">审批通过后系统会原子扣减库存；驳回需填写原因，申请人会收到提醒。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/borrow?action=allRecords" class="btn btn--ghost">查看借用台账</a>
                </div>
            </div>

            <c:if test="${not empty msg}">
                <div class="alert alert--ok">${fn:escapeXml(msg)}</div>
            </c:if>
            <c:if test="${not empty errMsg}">
                <div class="alert alert--danger">${fn:escapeXml(errMsg)}</div>
            </c:if>

            <section class="panel">
                <form class="toolbar" method="get" action="${ctx}/borrow">
                    <input type="hidden" name="action" value="pending">
                    <label class="search">
                        <span class="search__icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>
                        </span>
                        <input type="text" name="keyword" class="form-control" placeholder="搜索申请人或设备" value="${fn:escapeXml(keyword)}">
                    </label>
                    <button type="submit" class="btn btn--primary">搜索</button>
                    <a href="${ctx}/borrow?action=pending" class="btn btn--ghost">重置</a>
                    <span class="toolbar__spacer"></span>
                    <span class="tag tag--pending">待处理 ${total} 单</span>
                </form>

                <c:choose>
                    <c:when test="${empty recordList}">
                        <div class="panel__body">
                            <div class="empty">
                                <div class="empty__art">
                                    <svg width="84" height="84" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.1" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                                </div>
                                <div class="empty__title">待审批队列已清空</div>
                                <div class="empty__desc">所有借用申请都已处理完毕，做得不错</div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="panel__body">
                            <div class="record-list">
                                <c:forEach items="${recordList}" var="rec">
                                    <div class="record-item">
                                        <div style="display:flex;gap:14px;align-items:flex-start">
                                            <span class="avatar avatar--lg">
                                                <c:choose>
                                                    <c:when test="${empty rec.userName}">?</c:when>
                                                    <c:otherwise>${fn:substring(rec.userName, 0, 1)}</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <div>
                                                <div class="record-item__title">
                                                    ${fn:escapeXml(rec.userName)}
                                                    <span class="muted f-13">申请借用</span>
                                                    ${fn:escapeXml(rec.equipmentName)}
                                                </div>
                                                <div class="record-item__sub">
                                                    数量 ${rec.quantity} 件 ·
                                                    借用日期 <fmt:formatDate value="${rec.borrowTime}" pattern="yyyy-MM-dd"/> ·
                                                    预计归还 <fmt:formatDate value="${rec.expectReturnTime}" pattern="yyyy-MM-dd"/>
                                                </div>
                                                <c:if test="${not empty rec.remark}">
                                                    <div class="record-item__sub">用途：${fn:escapeXml(rec.remark)}</div>
                                                </c:if>
                                                <c:if test="${rec.remindCount gt 0}">
                                                    <div class="record-item__sub">已催还 ${rec.remindCount} 次</div>
                                                </c:if>
                                            </div>
                                        </div>

                                        <div class="record-item__right">
                                            <span class="tag tag--pending">待审批</span>
                                            <form action="${ctx}/borrow?action=audit" method="post"
                                                  onsubmit="return confirm('确认通过该申请？通过后将占用库存。')">
                                                <input type="hidden" name="id" value="${rec.id}">
                                                <input type="hidden" name="result" value="1">
                                                <input type="hidden" name="remark" value="同意借用，请按时归还">
                                                <button type="submit" class="btn btn--primary btn--sm">通过</button>
                                            </form>
                                            <button type="button" class="btn btn--danger btn--sm"
                                                    data-reject="${rec.id}">驳回</button>
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
                                    <a class="pager__item" href="${ctx}/borrow?action=pending&pageNum=${pageNum - 1}${qs}">上一页</a>
                                </c:when>
                                <c:otherwise><span class="pager__item is-disabled">上一页</span></c:otherwise>
                            </c:choose>
                            <c:forEach begin="1" end="${pageTotal}" var="i">
                                <c:choose>
                                    <c:when test="${i == pageNum}"><span class="pager__item is-active">${i}</span></c:when>
                                    <c:otherwise><a class="pager__item" href="${ctx}/borrow?action=pending&pageNum=${i}${qs}">${i}</a></c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <c:choose>
                                <c:when test="${pageNum lt pageTotal}">
                                    <a class="pager__item" href="${ctx}/borrow?action=pending&pageNum=${pageNum + 1}${qs}">下一页</a>
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

<%-- 驳回意见弹窗 --%>
<div class="modal-mask" id="rejectMask">
    <div class="modal">
        <form action="${ctx}/borrow?action=audit" method="post" id="rejectForm">
            <div class="modal__head">
                驳回申请
            </div>
            <div class="modal__body">
                <input type="hidden" name="id" id="rejectId">
                <input type="hidden" name="result" value="2">
                <div class="field">
                    <label for="rejectRemark">驳回原因 <span class="req">*</span></label>
                    <textarea id="rejectRemark" name="remark" class="form-control" rows="3" maxlength="200"
                              placeholder="例如：该时段设备已排满课程使用，请改期申请"></textarea>
                </div>
            </div>
            <div class="modal__foot">
                <button type="button" class="btn btn--ghost" id="rejectCancel">取消</button>
                <button type="submit" class="btn btn--danger">确认驳回</button>
            </div>
        </form>
    </div>
</div>

<script>
(function () {
    var mask = document.getElementById('rejectMask');
    var idInput = document.getElementById('rejectId');
    var remark = document.getElementById('rejectRemark');

    Array.prototype.forEach.call(document.querySelectorAll('[data-reject]'), function (btn) {
        btn.addEventListener('click', function () {
            idInput.value = btn.getAttribute('data-reject');
            remark.value = '';
            mask.classList.add('is-open');
            remark.focus();
        });
    });

    document.getElementById('rejectCancel').addEventListener('click', function () {
        mask.classList.remove('is-open');
    });

    mask.addEventListener('click', function (e) {
        if (e.target === mask) { mask.classList.remove('is-open'); }
    });

    document.getElementById('rejectForm').addEventListener('submit', function (e) {
        if (!remark.value.trim()) {
            e.preventDefault();
            alert('请填写驳回原因');
        }
    });
})();
</script>
</body>
</html>
