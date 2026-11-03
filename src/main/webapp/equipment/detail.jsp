<%-- ============================================================
     设备详情（左：设备图 + 库存；右：完整参数 + 最近借用）
     依赖：equipment、recentList
     注意：循环变量禁止用 eq / ne / lt / gt 等 EL 关键字（会被当作运算符）
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
<c:set var="activeNav" value="equipment-detail" scope="page"/>
<c:set var="item" value="${equipment}" scope="page"/>
<c:set var="pct" value="0" scope="page"/>
<c:if test="${item.stock gt 0}">
    <c:set var="pct" value="${item.available * 100 / item.stock}" scope="page"/>
</c:if>
<c:set var="barCls" value="bar bar--ok" scope="page"/>
<c:if test="${pct le 30}">
    <c:set var="barCls" value="bar bar--warn" scope="page"/>
</c:if>
<c:if test="${pct le 0}">
    <c:set var="barCls" value="bar bar--danger" scope="page"/>
</c:if>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${fn:escapeXml(item.eqName)} · 设备详情</title>
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
                    <h1>${fn:escapeXml(item.eqName)}</h1>
                    <p class="desc">设备编号 ${fn:escapeXml(item.id)}，查看完整参数与最近的借用记录。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/equipment?action=list" class="btn btn--ghost">返回设备列表</a>
                    <c:if test="${item.status == 0 and item.available gt 0}">
                        <a href="${ctx}/borrow?action=apply&id=${item.id}" class="btn btn--primary">申请借用</a>
                    </c:if>
                    <c:if test="${user.role == 1}">
                        <a href="${ctx}/equipment?action=edit&id=${item.id}" class="btn btn--ghost">编辑设备</a>
                    </c:if>
                </div>
            </div>

            <div class="detail-layout">
                <%-- 左栏：图片 + 库存 --%>
                <aside class="panel">
                    <div class="panel__body">
                        <div class="thumb-box">
                            <c:choose>
                                <c:when test="${not empty item.img}">
                                    <img src="${ctx}${fn:escapeXml(item.img)}" alt="${fn:escapeXml(item.eqName)}">
                                </c:when>
                                <c:otherwise>
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.1" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a4 4 0 0 1 5 5l-9.4 9.4a2.1 2.1 0 0 1-3-3z"/><path d="M17.4 8.4 15.6 6.6"/></svg>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="row-between" style="margin-top:16px">
                            <span class="stat__label">可借 / 总数</span>
                            <span>
                                <strong style="font-size:20px;color:var(--brand)">${item.available}</strong>
                                <span class="muted"> / ${item.stock} 件</span>
                            </span>
                        </div>
                        <div class="${barCls}" style="margin-top:8px"><i style="width:${pct}%"></i></div>

                        <div style="margin-top:16px">
                            <c:choose>
                                <c:when test="${item.status == 0}"><span class="tag tag--ok">正常可借</span></c:when>
                                <c:when test="${item.status == 1}"><span class="tag tag--warn">维修中</span></c:when>
                                <c:when test="${item.status == 2}"><span class="tag tag--plain">已报废</span></c:when>
                                <c:otherwise><span class="tag tag--plain">未知状态</span></c:otherwise>
                            </c:choose>
                            <span class="tag tag--plain" style="margin-left:6px">
                                <c:choose>
                                    <c:when test="${empty item.eqType}">未分类</c:when>
                                    <c:otherwise>${fn:escapeXml(item.eqType)}</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>
                </aside>

                <%-- 右栏：详情 + 记录 --%>
                <section>
                    <div class="panel">
                        <div class="panel__head">
                            <h2>设备参数</h2>
                        </div>
                        <div class="panel__body">
                            <dl class="detail-grid">
                                <dt>设备名称</dt><dd>${fn:escapeXml(item.eqName)}</dd>
                                <dt>设备编号</dt><dd>${fn:escapeXml(item.id)}</dd>
                                <dt>规格型号</dt>
                                <dd>
                                    <c:choose>
                                        <c:when test="${empty item.model}">—</c:when>
                                        <c:otherwise>${fn:escapeXml(item.model)}</c:otherwise>
                                    </c:choose>
                                </dd>
                                <dt>设备类别</dt>
                                <dd>
                                    <c:choose>
                                        <c:when test="${empty item.eqType}">—</c:when>
                                        <c:otherwise>${fn:escapeXml(item.eqType)}</c:otherwise>
                                    </c:choose>
                                </dd>
                                <dt>存放位置</dt>
                                <dd>
                                    <c:choose>
                                        <c:when test="${empty item.location}">未登记</c:when>
                                        <c:otherwise>${fn:escapeXml(item.location)}</c:otherwise>
                                    </c:choose>
                                </dd>
                                <dt>库房总数</dt><dd>${item.stock} 件</dd>
                                <dt>累计借出</dt><dd>${item.borrowCount} 次</dd>
                                <dt>备注说明</dt>
                                <dd>
                                    <c:choose>
                                        <c:when test="${empty item.remark}">—</c:when>
                                        <c:otherwise>${fn:escapeXml(item.remark)}</c:otherwise>
                                    </c:choose>
                                </dd>
                            </dl>
                        </div>
                    </div>

                    <div class="panel">
                        <div class="panel__head">
                            <h2>最近借用记录</h2>
                            <span class="sub">最多显示最近 5 条</span>
                        </div>
                        <c:choose>
                            <c:when test="${empty recentList}">
                                <div class="panel__body">
                                    <div class="empty">
                                        <div class="empty__title">暂无借用记录</div>
                                        <div class="empty__desc">这台设备还没有被借出过</div>
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-wrap">
                                    <table class="table">
                                        <thead>
                                        <tr>
                                            <th>借用人</th>
                                            <th class="num">数量</th>
                                            <th>借用时间</th>
                                            <th>预计归还</th>
                                            <th>实际归还</th>
                                            <th>状态</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach items="${recentList}" var="rec">
                                            <tr>
                                                <td class="strong">${fn:escapeXml(rec.userName)}</td>
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
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>
            </div>
        </main>
        <%@ include file="/common/footer.jsp" %>
    </div>
</div>
</body>
</html>
