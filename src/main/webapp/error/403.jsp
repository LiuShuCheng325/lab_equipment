<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>403 权限不足 - 实验室设备借用管理系统</title>
    <%@ include file="/common/css.jsp" %>
</head>
<body>

<div class="error-page">
    <div class="error-card">
        <div class="error-card__code">403</div>
        <div class="error-card__title">没有访问权限</div>
        <p class="error-card__desc">
            当前账号无权查看该页面。如需管理员权限，请联系实验室管理员开通；你也可以
            <a href="${ctx}/equipment?action=list">前往设备一览</a> 继续操作。
        </p>
        <div class="error-card__actions">
            <a href="${ctx}/dashboard" class="btn btn--primary btn--lg">返回首页</a>
            <a href="javascript:history.back()" class="btn btn--ghost btn--lg">返回上一页</a>
        </div>
    </div>
</div>

</body>
</html>
