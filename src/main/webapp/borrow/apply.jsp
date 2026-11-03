<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty sessionScope.user}">
    <c:redirect url="/login.jsp"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<c:set var="user" value="${sessionScope.user}" scope="page"/>
<c:set var="activeNav" value="borrow-apply" scope="page"/>
<c:set var="pageTitle" value="申请借用" scope="page"/>
<jsp:useBean id="today" class="java.util.Date" scope="page"/>
<jsp:useBean id="defaultDue" class="java.util.Date" scope="page"/>
<jsp:setProperty name="defaultDue" property="time" value="${today.time + 7 * 86400000}"/>
<c:set var="todayStr"><fmt:formatDate value="${today}" pattern="yyyy-MM-dd"/></c:set>
<c:set var="dueStr"><fmt:formatDate value="${defaultDue}" pattern="yyyy-MM-dd"/></c:set>
<c:set var="maxQty" value="${empty equipment.available ? 1 : equipment.available}" scope="page"/>

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
                    <p class="desc">请填写借用数量与预计归还日期，提交后由管理员审批。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/equipment?action=list" class="btn btn--ghost">返回设备一览</a>
                </div>
            </div>

            <div class="form-layout">
                <%-- ============ 左：申请表单 ============ --%>
                <section class="panel">
                    <div class="panel__head">
                        <h2>借用信息</h2>
                        <span class="sub">带 <span class="req">*</span> 为必填项</span>
                    </div>
                    <div class="panel__body">
                        <form method="post" action="${ctx}/borrow">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="equipmentId" value="${equipment.id}">

                            <div class="field">
                                <label>借用设备</label>
                                <div class="form-control" style="background:var(--surface-2);color:var(--ink)">
                                    ${fn:escapeXml(equipment.eqName)}
                                    <c:if test="${not empty equipment.model}">
                                        （${fn:escapeXml(equipment.model)}）
                                    </c:if>
                                </div>
                            </div>

                            <div class="field">
                                <label for="borrowNum">借用数量<span class="req">*</span></label>
                                <div class="stepper" data-stepper
                                     data-max="${maxQty}" data-target="borrowNum">
                                    <button type="button" class="stepper__btn" data-step="-1" aria-label="减少">−</button>
                                    <input type="number" id="borrowNum" name="borrowNum" class="stepper__input"
                                           value="1" min="1" max="${maxQty}" step="1">
                                    <button type="button" class="stepper__btn" data-step="1" aria-label="增加">+</button>
                                </div>
                                <div class="input-tip">
                                    当前可借 ${maxQty} 件，本次借用后剩余
                                    <strong class="remain remain--ok" id="remainTip">${maxQty - 1}</strong> 件
                                </div>
                            </div>

                            <div class="form-row">
                                <div class="field">
                                    <label for="borrowDate">借出日期</label>
                                    <input type="date" id="borrowDate" name="borrowDate"
                                           class="form-control" value="${todayStr}">
                                </div>
                                <div class="field">
                                    <label for="expectReturnDate">预计归还日期<span class="req">*</span></label>
                                    <input type="date" id="expectReturnDate" name="expectReturnDate"
                                           class="form-control" value="${dueStr}">
                                </div>
                            </div>

                            <div class="field">
                                <label for="remark">用途说明</label>
                                <textarea id="remark" name="remark" class="form-control"
                                          placeholder="例如：用于《软件工程》课程设计数据采集，使用地点为实验楼 A302"></textarea>
                                <div class="input-tip">最多 500 字</div>
                            </div>

                            <div class="actions">
                                <button type="submit" class="btn btn--primary btn--lg">提交借用申请</button>
                                <a href="${ctx}/equipment?action=list" class="btn btn--ghost btn--lg">取消</a>
                            </div>
                        </form>
                    </div>
                </section>

                <%-- ============ 右：申请须知 ============ --%>
                <div class="stack">
                    <div class="note-card">
                        <div class="note-card__title">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                                 stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="12" cy="12" r="9"/>
                                <path d="M12 16v-5M12 8h.01"/>
                            </svg>
                            申请须知
                        </div>
                        <ol>
                            <li>提交后状态为「待审批」，需管理员复核通过后才可领取设备。</li>
                            <li>默认借期为 7 天，到期前系统会推送提醒，请按时归还。</li>
                            <li>逾期未还会计入个人借用记录，影响后续审批结果。</li>
                            <li>归还时须如实选择设备状况（完好 / 损坏 / 丢失）。</li>
                            <li>设备使用中若发生故障，请立即在「我的报修」中登记。</li>
                        </ol>
                    </div>

                    <section class="panel">
                        <div class="panel__head">
                            <h2>设备概况</h2>
                        </div>
                        <div class="panel__body">
                            <dl class="detail-grid">
                                <dt>设备编号</dt>
                                <dd>#${equipment.id}</dd>
                                <dt>类别</dt>
                                <dd>${empty equipment.eqType ? '未分类' : fn:escapeXml(equipment.eqType)}</dd>
                                <dt>型号</dt>
                                <dd>${empty equipment.model ? '—' : fn:escapeXml(equipment.model)}</dd>
                                <dt>存放位置</dt>
                                <dd>${empty equipment.location ? '—' : fn:escapeXml(equipment.location)}</dd>
                                <dt>库存</dt>
                                <dd>可借 ${equipment.available} / 共 ${equipment.stock}</dd>
                            </dl>
                        </div>
                    </section>
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
    /* 数量步进器：限制 1 ~ max，并实时刷新「借用后剩余」 */
    (function () {
        var box = document.querySelector('[data-stepper]');
        if (!box) {
            return;
        }
        var input = document.getElementById(box.getAttribute('data-target'));
        var max = parseInt(box.getAttribute('data-max'), 10) || 1;
        var tip = document.getElementById('remainTip');
        var buttons = box.querySelectorAll('[data-step]');

        function refresh() {
            var value = parseInt(input.value, 10);
            if (isNaN(value) || value < 1) {
                value = 1;
            }
            if (value > max) {
                value = max;
            }
            input.value = value;
            if (tip) {
                var remain = max - value;
                tip.textContent = String(remain);
                tip.className = 'remain ' + (remain <= 0 ? 'remain--danger' : (remain <= 3 ? 'remain--warn' : 'remain--ok'));
            }
        }

        for (var i = 0; i < buttons.length; i++) {
            buttons[i].addEventListener('click', function () {
                var step = parseInt(this.getAttribute('data-step'), 10);
                var current = parseInt(input.value, 10);
                input.value = (isNaN(current) ? 1 : current) + step;
                refresh();
            });
        }
        input.addEventListener('input', refresh);
        refresh();
    })();
</script>
</body>
</html>
