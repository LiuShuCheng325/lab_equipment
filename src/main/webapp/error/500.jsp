<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>500 服务器内部错误 - 实验室设备借用管理系统</title>
    <%@ include file="/common/css.jsp" %>
</head>
<body>

<div class="error-page">
    <div class="error-card">
        <div class="error-card__code">500</div>
        <div class="error-card__title">服务器开小差了</div>
        <p class="error-card__desc">
            处理请求时出现了异常，我们已记录该问题。请稍后重试，或返回首页继续操作。
        </p>
        <div class="error-card__actions">
            <a href="${ctx}/dashboard" class="btn btn--primary btn--lg">返回首页</a>
            <a href="javascript:history.back()" class="btn btn--ghost btn--lg">返回上一页</a>
        </div>
    </div>
</div>

</body>
</html>
