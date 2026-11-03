<%-- ============================================================
     设备一览（卡片视图 / 表格视图 双视图切换）
     数据：equipmentList、total、pageNum、pageTotal、keyword、eqType、status
     规则：Tomcat 11 的 Jasper 会校验 JSP 属性中的 EL，
           因此 c:set / 属性值里禁止出现 "X ? a : b" 三元表达式，
           一律改用 c:choose 或预先算好的字符串变量。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty sessionScope.user}">
    <c:redirect url="/login.jsp"/>
</c:if>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<c:set var="user" value="${sessionScope.user}" scope="page"/>
<c:set var="activeNav" value="equipment" scope="page"/>
<c:set var="pageTitle" value="设备一览" scope="page"/>
<c:set var="qs" value="&keyword=${fn:escapeXml(keyword)}&eqType=${fn:escapeXml(eqType)}&status=${status}" scope="page"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>设备一览 · 实验室设备借用管理系统</title>
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
                    <h1>设备一览</h1>
                    <p class="desc">
                        共 <strong>${total}</strong> 台设备
                        <c:if test="${user.role != 1}">，仅「正常」且库存大于 0 的设备可提交借用申请</c:if>
                    </p>
                </div>
                <div class="actions">
                    <a href="${ctx}/export?type=equipment" class="btn btn--ghost">导出设备表</a>
                    <c:if test="${user.role == 1}">
                        <a href="${ctx}/equipment?action=add" class="btn btn--primary">+ 新增设备</a>
                    </c:if>
                </div>
            </div>

            <section class="panel">
                <%-- 工具栏 --%>
                <form class="toolbar" method="get" action="${ctx}/equipment">
                    <input type="hidden" name="action" value="list">

                    <label class="search">
                        <span class="search__icon">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                                 stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>
                            </svg>
                        </span>
                        <input type="text" name="keyword" class="form-control" placeholder="搜索设备名称 / 编号 / 型号"
                               value="${fn:escapeXml(keyword)}">
                    </label>

                    <label class="field" style="margin:0">
                        <select name="eqType" class="select">
                            <option value="">全部类别</option>
                            <c:forEach items="${categoryList}" var="cat">
                                <option value="${fn:escapeXml(cat)}">${fn:escapeXml(cat)}</option>
                            </c:forEach>
                        </select>
                    </label>

                    <label class="field" style="margin:0">
                        <select name="status" class="select">
                            <option value="">全部状态</option>
                            <option value="0">正常</option>
                            <option value="1">维修中</option>
                            <option value="2">已报废</option>
                        </select>
                    </label>

                    <button type="submit" class="btn btn--primary">筛选</button>
                    <a href="${ctx}/equipment?action=list" class="btn btn--ghost">重置</a>

                    <span class="toolbar__spacer"></span>

                    <span class="seg" role="group" aria-label="视图切换">
                        <button type="button" class="seg__btn is-active" data-view="card">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/>
                                <rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>
                            </svg>
                            卡片
                        </button>
                        <button type="button" class="seg__btn" data-view="list">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M8 6h13M8 12h13M8 18h13"/><path d="M3 6h.01M3 12h.01M3 18h.01"/>
                            </svg>
                            列表
                        </button>
                    </span>
                </form>

                <c:choose>
                    <c:when test="${empty equipmentList}">
                        <div class="panel__body">
                            <div class="empty">
                                <div class="empty__art">
                                    <svg width="88" height="88" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                         stroke-width="1.1" stroke-linecap="round" stroke-linejoin="round">
                                        <circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>
                                    </svg>
                                </div>
                                <div class="empty__title">没有找到符合条件的设备</div>
                                <div class="empty__desc">试试更换关键字或清空筛选条件</div>
                                <a href="${ctx}/equipment?action=list" class="btn btn--ghost empty__action">清空筛选</a>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <%-- ============ 卡片视图 ============ --%>
                        <div class="panel__body" id="eqView-card">
                            <div class="card-grid">
                                <c:forEach items="${equipmentList}" var="item">
                                    <c:set var="pct" value="0" scope="page"/>
                                    <c:if test="${item.stock gt 0}">
                                        <c:set var="pct" value="${item.available * 100 / item.stock}" scope="page"/>
                                    </c:if>
                                    <c:set var="barCls" value="bar bar--ok" scope="page"/>
                                    <c:if test="${pct <= 30}">
                                        <c:set var="barCls" value="bar bar--warn" scope="page"/>
                                    </c:if>
                                    <c:if test="${pct <= 0}">
                                        <c:set var="barCls" value="bar bar--danger" scope="page"/>
                                    </c:if>
                                    <c:set var="barW" value="${pct}" scope="page"/>
                                    <c:if test="${pct < 2}">
                                        <c:set var="barW" value="2" scope="page"/>
                                    </c:if>

                                    <article class="eq-card">
                                        <div class="eq-card__thumb">
                                            <c:choose>
                                                <c:when test="${not empty item.img}">
                                                    <img src="${ctx}${fn:escapeXml(item.img)}" alt="${fn:escapeXml(item.eqName)}">
                                                </c:when>
                                                <c:when test="${fn:contains(item.eqType, '光学')}">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="10" r="5.2"/><circle cx="11" cy="10" r="1.8"/><path d="M11 15.2V21M7.5 21h7M18.8 5.5 21 3.3M15.4 5.2l1.6-1.6"/></svg>
                                                </c:when>
                                                <c:when test="${fn:contains(item.eqType, '称量')}">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20.5h16M6 20.5V9l6-4 6 4v11.5"/><path d="M9.2 13h5.6"/></svg>
                                                </c:when>
                                                <c:when test="${fn:contains(item.eqType, '离心')}">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="8.2"/><circle cx="12" cy="12" r="2.4"/><path d="M12 3.8v3M12 17.2v3M3.8 12h3M17.2 12h3"/></svg>
                                                </c:when>
                                                <c:when test="${fn:contains(item.eqType, '分子')}">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"><path d="M7 3c0 8 10 10 10 18M17 3c0 8-10 10-10 18"/><path d="M8.6 7h6.8M8 12h8M8.6 17h6.8"/></svg>
                                                </c:when>
                                                <c:when test="${fn:contains(item.eqType, '生物')}">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"><path d="M7 3c0 8 10 10 10 18M17 3c0 8-10 10-10 18"/><path d="M8.6 7h6.8M8 12h8M8.6 17h6.8"/></svg>
                                                </c:when>
                                                <c:when test="${fn:contains(item.eqType, '灭菌')}">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="6" width="14" height="15" rx="2.2"/><path d="M9 6V4h6v2M9.5 12h5M9.5 16h5"/></svg>
                                                </c:when>
                                                <c:when test="${fn:contains(item.eqType, '无菌')}">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="6" width="14" height="15" rx="2.2"/><path d="M9 6V4h6v2M9.5 12h5M9.5 16h5"/></svg>
                                                </c:when>
                                                <c:when test="${fn:contains(item.eqType, '电子')}">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"><rect x="2.8" y="5" width="18.4" height="14" rx="2.2"/><path d="M6 13.5 8.6 10l2.2 3.4 2-2.6 2.2 3.2"/><path d="M6.8 9h2.4"/></svg>
                                                </c:when>
                                                <c:when test="${fn:contains(item.eqType, '测量')}">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"><rect x="2.8" y="5" width="18.4" height="14" rx="2.2"/><path d="M6 13.5 8.6 10l2.2 3.4 2-2.6 2.2 3.2"/><path d="M6.8 9h2.4"/></svg>
                                                </c:when>
                                                <c:otherwise>
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a4 4 0 0 1 5 5l-9.4 9.4a2.1 2.1 0 0 1-3-3z"/><path d="M6.6 6.6 4 4"/></svg>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

                                        <div class="eq-card__body">
                                            <div class="row-between">
                                                <span class="tag tag--plain">
                                                    <c:choose>
                                                        <c:when test="${empty item.eqType}">未分类</c:when>
                                                        <c:otherwise>${fn:escapeXml(item.eqType)}</c:otherwise>
                                                    </c:choose>
                                                </span>
                                                <span class="eq-card__status">
                                                    <c:choose>
                                                        <c:when test="${item.status == 0}"><span class="tag tag--ok">正常</span></c:when>
                                                        <c:when test="${item.status == 1}"><span class="tag tag--warn">维修中</span></c:when>
                                                        <c:when test="${item.status == 2}"><span class="tag tag--plain">已报废</span></c:when>
                                                        <c:otherwise><span class="tag tag--plain">未知</span></c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>

                                            <div class="eq-card__name">${fn:escapeXml(item.eqName)}</div>
                                            <div class="eq-card__model">
                                                <c:choose>
                                                    <c:when test="${empty item.model}">—</c:when>
                                                    <c:otherwise>${fn:escapeXml(item.model)}</c:otherwise>
                                                </c:choose>
                                            </div>

                                            <div class="eq-card__meta">
                                                <div class="eq-card__meta-row">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                                                        <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0z"/><circle cx="12" cy="10" r="3"/>
                                                    </svg>
                                                    <span>
                                                        <c:choose>
                                                            <c:when test="${empty item.location}">未登记位置</c:when>
                                                            <c:otherwise>${fn:escapeXml(item.location)}</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                </div>
                                                <div class="eq-card__meta-row">
                                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                                                        <path d="M20 6 9 17l-5-5"/>
                                                    </svg>
                                                    <span>累计借出 ${item.borrowCount} 次</span>
                                                </div>
                                            </div>

                                            <div class="eq-card__stock">
                                                <div class="eq-card__stock-top">
                                                    <span>可借 ${item.available} / 共 ${item.stock}</span>
                                                    <span>${fn:substringBefore(pct, '.')}%</span>
                                                </div>
                                                <div class="${barCls}"><i style="width:${barW}%"></i></div>
                                            </div>

                                            <div class="eq-card__actions">
                                                <a href="${ctx}/equipment?action=detail&id=${item.id}" class="btn btn--ghost btn--sm">详情</a>
                                                <c:if test="${item.status == 0 and item.available gt 0}">
                                                    <a href="${ctx}/borrow?action=apply&id=${item.id}" class="btn btn--primary btn--sm">申请借用</a>
                                                </c:if>
                                                <c:if test="${user.role == 1}">
                                                    <a href="${ctx}/equipment?action=edit&id=${item.id}" class="btn btn--sm">编辑</a>
                                                </c:if>
                                            </div>
                                        </div>
                                    </article>
                                </c:forEach>
                            </div>
                        </div>

                        <%-- ============ 表格视图 ============ --%>
                        <div class="is-hidden" id="eqView-list">
                            <div class="table-wrap">
                                <table class="table">
                                    <thead>
                                    <tr>
                                        <th>设备名称</th>
                                        <th>型号</th>
                                        <th>类别</th>
                                        <th>存放位置</th>
                                        <th class="num">可借 / 总数</th>
                                        <th>状态</th>
                                        <th class="text-right">操作</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${equipmentList}" var="item">
                                        <tr>
                                            <td class="strong">${fn:escapeXml(item.eqName)}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${empty item.model}">—</c:when>
                                                    <c:otherwise>${fn:escapeXml(item.model)}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${empty item.eqType}">—</c:when>
                                                    <c:otherwise>${fn:escapeXml(item.eqType)}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${empty item.location}">—</c:when>
                                                    <c:otherwise>${fn:escapeXml(item.location)}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="num">${item.available} / ${item.stock}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${item.status == 0}"><span class="tag tag--ok">正常</span></c:when>
                                                    <c:when test="${item.status == 1}"><span class="tag tag--warn">维修中</span></c:when>
                                                    <c:when test="${item.status == 2}"><span class="tag tag--plain">已报废</span></c:when>
                                                    <c:otherwise><span class="tag tag--plain">未知</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <div class="actions">
                                                    <a href="${ctx}/equipment?action=detail&id=${item.id}" class="btn btn--ghost btn--sm">详情</a>
                                                    <c:if test="${item.status == 0 and item.available gt 0}">
                                                        <a href="${ctx}/borrow?action=apply&id=${item.id}" class="btn btn--primary btn--sm">申请借用</a>
                                                    </c:if>
                                                    <c:if test="${user.role == 1}">
                                                        <a href="${ctx}/equipment?action=edit&id=${item.id}" class="btn btn--sm">编辑</a>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
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
                                    <a class="pager__item" href="${ctx}/equipment?action=list&pageNum=${pageNum - 1}${qs}">上一页</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="pager__item is-disabled">上一页</span>
                                </c:otherwise>
                            </c:choose>
                            <c:forEach begin="1" end="${pageTotal}" var="i">
                                <c:choose>
                                    <c:when test="${i == pageNum}">
                                        <span class="pager__item is-active">${i}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="pager__item" href="${ctx}/equipment?action=list&pageNum=${i}${qs}">${i}</a>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                            <c:choose>
                                <c:when test="${pageNum < pageTotal}">
                                    <a class="pager__item" href="${ctx}/equipment?action=list&pageNum=${pageNum + 1}${qs}">下一页</a>
                                </c:when>
                                <c:otherwise>
                                    <span class="pager__item is-disabled">下一页</span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </c:if>
            </section>
        </main>
        <%@ include file="/common/footer.jsp" %>
    </div>
</div>

<div class="toast-host" id="toastHost"
     data-msg="${fn:escapeXml(msg)}" data-err="${fn:escapeXml(errMsg)}"></div>

<script src="${ctx}/js/app.js?v=4.3"></script>
<script>
(function () {
    var buttons = document.querySelectorAll('[data-view]');
    var card = document.getElementById('eqView-card');
    var list = document.getElementById('eqView-list');
    for (var i = 0; i < buttons.length; i++) {
        buttons[i].addEventListener('click', function () {
            var view = this.getAttribute('data-view');
            for (var j = 0; j < buttons.length; j++) {
                buttons[j].classList.remove('is-active');
            }
            this.classList.add('is-active');
            if (card) { card.classList.toggle('is-hidden', view !== 'card'); }
            if (list) { list.classList.toggle('is-hidden', view !== 'list'); }
        });
    }

    /* 状态下拉回显 */
    var sel = document.querySelector('select[name="status"]');
    if (sel) {
        var want = '${status}';
        for (var k = 0; k < sel.options.length; k++) {
            if (sel.options[k].value === want) { sel.selectedIndex = k; }
        }
    }
    var selType = document.querySelector('select[name="eqType"]');
    if (selType) {
        var wantType = '${fn:escapeXml(eqType)}';
        for (var m = 0; m < selType.options.length; m++) {
            if (selType.options[m].value === wantType) { selType.selectedIndex = m; }
        }
    }
})();
</script>
</body>
</html>
