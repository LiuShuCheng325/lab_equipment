<%-- ============================================================
     公共样式入口：全站唯一视觉来源。
     所有样式统一写在 css/style.css（design token + 组件），
     此处只做一次引用，避免各页面出现第二套冲突样式。
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="page"/>
<link rel="stylesheet" href="${ctx}/css/style.css?v=4.3">
