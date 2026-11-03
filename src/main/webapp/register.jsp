<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>用户注册 · 实验室设备借用管理系统</title>
    <%@ include file="/common/css.jsp" %>
</head>
<body>

<div class="auth-layout">
    <%-- ============ 左侧品牌区 ============ --%>
    <section class="auth-brand">
        <div class="auth-brand__deco" aria-hidden="true">
            <svg viewBox="0 0 400 600" preserveAspectRatio="xMidYMid slice" fill="none"
                 stroke="currentColor" stroke-width="1.2">
                <circle cx="90" cy="120" r="130"/>
                <circle cx="90" cy="120" r="80"/>
                <circle cx="340" cy="500" r="140"/>
                <circle cx="340" cy="500" r="88"/>
                <path d="M0 160h400M0 380h400"/>
                <path d="M120 0v600M260 0v600"/>
            </svg>
        </div>

        <div class="auth-brand__head">
            <div class="auth-brand__logo">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9"
                     stroke-linecap="round" stroke-linejoin="round">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M19 8v6M22 11h-6"/>
                </svg>
            </div>
            <div class="auth-brand__title">创建你的账号</div>
            <div class="auth-brand__subtitle">注册后可在线申请借用实验室设备</div>
        </div>

        <div class="auth-brand__points">
            <div class="auth-point">
                <span class="auth-point__icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                         stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                </span>
                <span>台账清晰 · 随时查看可借库存</span>
            </div>
            <div class="auth-point">
                <span class="auth-point__icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                         stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                </span>
                <span>进度透明 · 审批结果与借期可查</span>
            </div>
            <div class="auth-point">
                <span class="auth-point__icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
                         stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
                </span>
                <span>到期提醒 · 不再错过归还日期</span>
            </div>
        </div>

        <div class="auth-brand__foot">昆明学院 · 软件工程 · 课程设计作品</div>
    </section>

    <%-- ============ 右侧表单区 ============ --%>
    <section class="auth-form">
        <div class="auth-form__inner">
            <h1 class="auth-form__title">用户注册</h1>
            <p class="auth-form__desc">请填写以下信息完成注册，带 <span class="req">*</span> 为必填项</p>

            <c:if test="${not empty msg}">
                <div class="alert alert--danger">${fn:escapeXml(msg)}</div>
            </c:if>
            <c:if test="${not empty successMsg}">
                <div class="alert alert--ok">${fn:escapeXml(successMsg)}</div>
            </c:if>

            <form id="registerForm" action="${ctx}/register" method="post" onsubmit="return validateForm()">
                <div class="field">
                    <label for="username">用户名<span class="req">*</span></label>
                    <input type="text" id="username" name="username" class="form-control"
                           placeholder="4-16 位字母或数字" autocomplete="username">
                    <div class="error-tip" id="usernameTip">用户名格式错误，仅支持 4-16 位字母数字</div>
                </div>

                <div class="field">
                    <label for="password">登录密码<span class="req">*</span></label>
                    <input type="password" id="password" name="password" class="form-control"
                           placeholder="6-20 位字符" autocomplete="new-password">
                    <div class="error-tip" id="passwordTip">密码长度必须 6~20 位</div>
                </div>

                <div class="field">
                    <label for="repPwd">确认密码<span class="req">*</span></label>
                    <input type="password" id="repPwd" class="form-control"
                           placeholder="请再次输入密码" autocomplete="new-password">
                    <div class="error-tip" id="repPwdTip">两次输入的密码不一致</div>
                </div>

                <div class="field">
                    <label for="name">真实姓名<span class="req">*</span></label>
                    <input type="text" id="name" name="name" class="form-control" placeholder="请输入真实姓名">
                    <div class="error-tip" id="nameTip">姓名不能为空</div>
                </div>

                <div class="field">
                    <label for="phone">手机号码</label>
                    <input type="text" id="phone" name="phone" class="form-control"
                           placeholder="选填，11 位中国大陆手机号">
                    <div class="error-tip" id="phoneTip">手机号格式不正确</div>
                </div>

                <div class="field">
                    <label for="email">电子邮箱</label>
                    <input type="email" id="email" name="email" class="form-control"
                           placeholder="选填，如 name@kmu.edu.cn">
                    <div class="error-tip" id="emailTip">邮箱格式不正确</div>
                </div>

                <div class="row-between mt-8">
                    <button type="submit" class="btn btn--primary btn--lg">立即注册</button>
                    <button type="reset" class="btn btn--ghost btn--lg">重置</button>
                </div>
            </form>

            <p class="input-tip mt-16">已有账号？<a href="${ctx}/login.jsp">前往登录</a></p>
        </div>
    </section>
</div>

<script>
    const regRules = {
        username: /^[a-zA-Z0-9]{4,16}$/,
        password: /^.{6,20}$/,
        phone: /^1[3-9]\d{9}$/,
        email: /^[\w.-]+@[\w.-]+\.\w{2,}$/
    };

    function showTip(id) {
        document.getElementById(id).style.display = "block";
    }

    function hideTip(id) {
        document.getElementById(id).style.display = "none";
    }

    function validateForm() {
        let pass = true;
        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value.trim();
        const repPwd = document.getElementById("repPwd").value.trim();
        const name = document.getElementById("name").value.trim();
        const phone = document.getElementById("phone").value.trim();
        const email = document.getElementById("email").value.trim();

        ["usernameTip", "passwordTip", "repPwdTip", "nameTip", "phoneTip", "emailTip"].forEach(hideTip);
        document.querySelectorAll(".form-control").forEach(el => el.classList.remove("error"));

        if (!regRules.username.test(username)) {
            showTip("usernameTip");
            document.getElementById("username").classList.add("error");
            pass = false;
        }
        if (!regRules.password.test(password)) {
            showTip("passwordTip");
            document.getElementById("password").classList.add("error");
            pass = false;
        }
        if (password !== repPwd) {
            showTip("repPwdTip");
            document.getElementById("repPwd").classList.add("error");
            pass = false;
        }
        if (name === "") {
            showTip("nameTip");
            document.getElementById("name").classList.add("error");
            pass = false;
        }
        if (phone !== "" && !regRules.phone.test(phone)) {
            showTip("phoneTip");
            document.getElementById("phone").classList.add("error");
            pass = false;
        }
        if (email !== "" && !regRules.email.test(email)) {
            showTip("emailTip");
            document.getElementById("email").classList.add("error");
            pass = false;
        }
        return pass;
    }

    document.querySelectorAll(".form-control").forEach(input => {
        input.addEventListener("input", function () {
            this.classList.remove("error");
            const tip = document.getElementById(this.id + "Tip");
            if (tip) {
                tip.style.display = "none";
            }
        });
    });
</script>
</body>
</html>
