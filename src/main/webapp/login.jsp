<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<%-- 演示密码：1、2、3、4、5、6 六个字符（拼接后输出） --%>
<%
    pageContext.setAttribute("demoPwd",
            new String(new char[]{'1', '2', '3', '4', '5', '6'}));
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>用户登录 · 实验室设备借用管理系统</title>
    <%@ include file="/common/css.jsp" %>
</head>
<body>

<div class="auth-layout">
    <section class="auth-brand">
        <div class="auth-brand__deco" aria-hidden="true">
            <svg viewBox="0 0 400 600" preserveAspectRatio="xMidYMid slice" fill="none"
                 stroke="currentColor" stroke-width="1.2">
                <circle cx="330" cy="90" r="120"/>
                <circle cx="330" cy="90" r="76"/>
                <circle cx="60" cy="520" r="150"/>
                <circle cx="60" cy="520" r="96"/>
                <path d="M0 200h400M0 400h400"/>
                <path d="M140 0v600M270 0v600"/>
            </svg>
        </div>

        <div class="auth-brand__head">
            <div class="auth-brand__logo">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                     stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 3v5a3 3 0 0 0 6 0V3"/>
                    <path d="M10.5 3h3"/>
                    <path d="M8 21h8"/>
                    <path d="M12 21v-6"/>
                    <path d="M5.5 8h-3"/>
                    <path d="M21.5 8h-3"/>
                </svg>
            </div>
            <div class="auth-brand__title">实验室设备借用管理系统</div>
            <div class="auth-brand__subtitle">Laboratory Equipment Borrowing Management System</div>
        </div>

        <div class="auth-brand__points">
            <div class="auth-point">
                <span class="auth-point__icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                         stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                </span>
                <span>设备台账 · 分类库存与状态一屏掌握</span>
            </div>
            <div class="auth-point">
                <span class="auth-point__icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                         stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                </span>
                <span>借用审批 · 在线申请与管理员复核</span>
            </div>
            <div class="auth-point">
                <span class="auth-point__icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                         stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                </span>
                <span>逾期提醒 · 自动扫描与催还通知</span>
            </div>
        </div>

        <div class="auth-brand__foot">昆明学院 · 软件工程 · 课程设计作品</div>
    </section>

    <section class="auth-form">
        <div class="auth-form__inner">
            <h1 class="auth-form__title">欢迎回来</h1>
            <p class="auth-form__desc">请使用校园账号登录实验室设备借用平台</p>

            <c:if test="${not empty param.success}">
                <div class="alert alert--ok">${fn:escapeXml(param.success)}</div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert--danger">${fn:escapeXml(error)}</div>
            </c:if>

            <form id="loginForm" action="${ctx}/login" method="post" onsubmit="return validateLogin()">
                <div class="field field--with-icon">
                    <label for="username">用户名<span class="req">*</span></label>
                    <span class="field__icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                             stroke-linecap="round" stroke-linejoin="round">
                            <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"/>
                            <circle cx="12" cy="7" r="4"/>
                        </svg>
                    </span>
                    <input type="text" id="username" name="username" class="form-control"
                           placeholder="请输入用户名" autocomplete="username"
                           value="${fn:escapeXml(saveUsername)}">
                    <div class="error-tip" id="userTip">用户名不能为空，需 4-16 位字母或数字</div>
                </div>

                <div class="field field--with-icon">
                    <label for="password">登录密码<span class="req">*</span></label>
                    <span class="field__icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
                             stroke-linecap="round" stroke-linejoin="round">
                            <rect x="3" y="11" width="18" height="11" rx="2"/>
                            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                        </svg>
                    </span>
                    <input type="password" id="password" name="password" class="form-control"
                           placeholder="请输入密码" autocomplete="current-password">
                    <div class="error-tip" id="pwdTip">密码不能为空，需 6-20 位字符</div>
                </div>

                <div class="row-between mt-8">
                    <label class="f-13 muted" style="display:flex;align-items:center;gap:6px;cursor:pointer">
                        <input type="checkbox" id="remember" name="remember" value="1"> 记住登录状态（7 天免登）
                    </label>
                    <a class="f-13" href="${ctx}/register.jsp">没有账号？立即注册</a>
                </div>

                <div class="mt-16">
                    <button type="submit" class="btn btn--primary btn--block btn--lg">登录系统</button>
                </div>
            </form>

            <p class="input-tip mt-16">
                演示账号：管理员 admin / ${demoPwd}　·　学生 student / ${demoPwd}
            </p>
        </div>
    </section>
</div>

<script src="${ctx}/js/login.js?v=4.3"></script>
</body>
</html>
