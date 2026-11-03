<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>400 请求参数错误 - 实验室设备借用管理系统</title>
    <%@ include file="/common/css.jsp" %>
</head>
<body>

<div class="error-page">
    <div class="error-card">
        <div class="error-card__code">400</div>
        <div class="error-card__title">请求参数有误</div>
        <p class="error-card__desc">
            请求中缺少必要参数或参数格式不正确，请返回上一页重新操作。
        </p>
        <div class="error-card__actions">
            <a href="${ctx}/dashboard" class="btn btn--primary btn--lg">返回首页</a>
            <a href="javascript:history.back()" class="btn btn--ghost btn--lg">返回上一页</a>
        </div>
    </div>
</div>

</body>
</html>
