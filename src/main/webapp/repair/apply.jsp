<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- 权限校验统一由 RepairServlet 处理，本页面只做展示 --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<c:set var="user" value="${sessionScope.user}" scope="page"/>
<c:set var="activeNav" value="repair-add" scope="page"/>
<c:set var="pageTitle" value="提交报修" scope="page"/>

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
                    <p class="desc">设备编号 #${equipmentId}。请详细描述故障现象，便于管理员安排维修。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/repair?action=myRepairs" class="btn btn--ghost">返回我的报修</a>
                </div>
            </div>

            <div class="form-layout">
                <%-- ============ 表单 ============ --%>
                <section class="panel">
                    <div class="panel__head">
                        <h2>报修信息</h2>
                        <span class="sub">带 <span class="req">*</span> 为必填项</span>
                    </div>

                    <div class="panel__body">
                        <c:if test="${not empty errMsg}">
                            <div class="alert alert--danger">${fn:escapeXml(errMsg)}</div>
                        </c:if>

                        <form method="post" action="${ctx}/repair" id="repairApplyForm">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="equipmentId" value="${equipmentId}">

                            <div class="field">
                                <label for="description">故障描述 <span class="req">*</span></label>
                                <textarea id="description" name="description" class="form-control" rows="5"
                                          maxlength="300" required
                                          placeholder="例如：离心机启动后异响明显，转速无法达到设定值，面板显示 E02 错误码"></textarea>
                                <div class="input-tip">
                                    请尽量写清故障现象、出现时机与已尝试的处理方式（还可输入
                                    <span id="descLeft">300</span> 字）
                                </div>
                            </div>

                            <div class="actions">
                                <button type="submit" class="btn btn--primary btn--lg">提交报修</button>
                                <a href="${ctx}/repair?action=myRepairs" class="btn btn--ghost btn--lg">取消</a>
                            </div>
                        </form>
                    </div>
                </section>

                <%-- ============ 须知 ============ --%>
                <div class="stack">
                    <div class="note-card">
                        <div class="note-card__title">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                                 stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="12" cy="12" r="9"/>
                                <path d="M12 16v-5M12 8h.01"/>
                            </svg>
                            报修须知
                        </div>
                        <ol>
                            <li>设备使用中发生故障请立即停止使用并登记报修。</li>
                            <li>若为人为操作不当造成损坏，需按实验室相关规定处理。</li>
                            <li>管理员处理完成后会在「我的报修」中更新状态与处理意见。</li>
                            <li>紧急情况请直接联系实验室值班教师。</li>
                        </ol>
                    </div>
                </div>
            </div>
        </div>

        <%@ include file="/common/footer.jsp" %>
    </div>
</div>

<div class="toast-host" id="toastHost"
     data-msg="${fn:escapeXml(msg)}" data-err="${fn:escapeXml(errMsg)}"></div>
<script src="${ctx}/js/app.js?v=4.3"></script>
<script>
    /* 故障描述剩余字数实时提示 */
    (function () {
        var area = document.getElementById('description');
        var left = document.getElementById('descLeft');
        if (!area || !left) {
            return;
        }
        var max = 300;
        function refresh() {
            left.textContent = String(Math.max(0, max - area.value.length));
        }
        area.addEventListener('input', refresh);
        refresh();
    })();
</script>
</body>
</html>
