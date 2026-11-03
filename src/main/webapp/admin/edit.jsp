<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- EL变量类型声明 --%>
<%--@elvariable id="editUser" type="com.lab.entity.User"--%>
<%-- 权限校验统一由 UserManageServlet 处理，本页面只做展示 --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<c:set var="user" value="${sessionScope.user}" scope="page"/>
<c:set var="activeNav" value="admin-user" scope="page"/>
<c:set var="pageTitle" value="编辑用户" scope="page"/>

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
                    <p class="desc">修改账号资料与角色权限，用户名作为登录标识不可修改。</p>
                </div>
                <div class="actions">
                    <a href="${ctx}/admin/user" class="btn btn--ghost">返回列表</a>
                </div>
            </div>

            <div class="form-layout">
                <%-- ============ 表单 ============ --%>
                <section class="panel">
                    <div class="panel__head">
                        <h2>账号信息</h2>
                        <span class="sub">账号编号 #${editUser.id}</span>
                    </div>

                    <div class="panel__body">
                        <c:if test="${not empty errMsg}">
                            <div class="alert alert--danger">${fn:escapeXml(errMsg)}</div>
                        </c:if>
                        <c:if test="${not empty error}">
                            <div class="alert alert--danger">${fn:escapeXml(error)}</div>
                        </c:if>

                        <c:choose>
                            <c:when test="${empty editUser}">
                                <div class="empty">
                                    <div class="empty__art">
                                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor"
                                             stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round">
                                            <circle cx="12" cy="12" r="9"/>
                                            <path d="M12 8v4M12 16h.01"/>
                                        </svg>
                                    </div>
                                    <div class="empty__title">账号不存在或已被删除</div>
                                    <div class="empty__desc">请返回用户管理列表重新选择要编辑的账号</div>
                                    <a href="${ctx}/admin/user" class="btn btn--primary empty__action">返回列表</a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${ctx}/admin/user" id="userEditForm">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="id" value="${editUser.id}">

                                    <div class="field">
                                        <label for="username">用户名</label>
                                        <input type="text" id="username" class="form-control"
                                               value="${fn:escapeXml(editUser.username)}" disabled>
                                        <span class="input-tip">用户名是登录标识，注册后不可修改</span>
                                    </div>

                                    <div class="field">
                                        <label for="name">真实姓名 <span class="req">*</span></label>
                                        <input type="text" id="name" name="name" class="form-control"
                                               maxlength="30" required
                                               value="${fn:escapeXml(editUser.name)}"
                                               placeholder="请填写真实姓名，便于设备交接">
                                    </div>

                                    <div class="form-row">
                                        <div class="form-group">
                                            <label for="phone">联系电话</label>
                                            <input type="text" id="phone" name="phone" class="form-control"
                                                   maxlength="20" value="${fn:escapeXml(editUser.phone)}"
                                                   placeholder="例：13800001111">
                                            <span class="input-tip">逾期催还会通过该手机号联系</span>
                                        </div>

                                        <div class="form-group">
                                            <label for="email">电子邮箱</label>
                                            <input type="email" id="email" name="email" class="form-control"
                                                   maxlength="60" value="${fn:escapeXml(editUser.email)}"
                                                   placeholder="例：name@example.com">
                                            <span class="input-tip">用于接收系统通知（选填）</span>
                                        </div>
                                    </div>

                                    <div class="field">
                                        <label for="role">账号角色 <span class="req">*</span></label>
                                        <select id="role" name="role" class="select" required>
                                            <option value="0" ${editUser.role == 0 ? 'selected' : ''}>借用用户（可申请借用 / 报修）</option>
                                            <option value="1" ${editUser.role == 1 ? 'selected' : ''}>管理员（可审批 / 维护设备 / 管理账号）</option>
                                        </select>
                                        <span class="input-tip">管理员拥有全部管理权限，请谨慎分配</span>
                                    </div>

                                    <div class="actions" style="margin-top:24px">
                                        <button type="submit" class="btn btn--primary btn--lg">保存修改</button>
                                        <a href="${ctx}/admin/user" class="btn btn--ghost btn--lg">取消</a>
                                    </div>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </section>

                <%-- ============ 说明 ============ --%>
                <div class="stack">
                    <div class="note-card">
                        <div class="note-card__title">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                                 stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="12" cy="12" r="9"/>
                                <path d="M12 16v-4M12 8h.01"/>
                            </svg>
                            编辑须知
                        </div>
                        <ol>
                            <li>带 <span class="req">*</span> 为必填项，保存前请确认信息准确。</li>
                            <li>用户名不可修改；如需更换，请注册新账号后删除旧账号。</li>
                            <li>角色下调为「借用用户」后，该账号将立即失去审批与维护权限。</li>
                            <li>删除账号不会清除其历史借用记录，台账数据仍可追溯。</li>
                        </ol>
                    </div>

                    <section class="panel">
                        <div class="panel__head">
                            <h2>当前账号</h2>
                        </div>
                        <div class="panel__body">
                            <dl class="detail-grid">
                                <dt>姓名</dt>
                                <dd>${fn:escapeXml(editUser.name)}</dd>
                                <dt>用户名</dt>
                                <dd>${fn:escapeXml(editUser.username)}</dd>
                                <dt>角色</dt>
                                <dd>
                                    <c:choose>
                                        <c:when test="${editUser.role == 1}">
                                            <span class="tag tag--approved">管理员</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="tag tag--plain">借用用户</span>
                                        </c:otherwise>
                                    </c:choose>
                                </dd>
                                <dt>操作者</dt>
                                <dd>${fn:escapeXml(user.name)}</dd>
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
</body>
</html>
