<%-- ============================================================
     公共分页片段（可复用）
     依赖变量：
       baseUrl  —— 分页基础地址，例如 ${ctx}/borrow?action=myRecords
                   或 ${ctx}/admin/user（不带查询串时会自动补 ?）
       pageNum  —— 当前页码
       pageTotal—— 总页数
       total    —— 总记录数
       qs       —— 可选，追加的筛选参数，形如 &keyword=xxx&status=1
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pagerQs" value="${empty qs ? '' : qs}" scope="page"/>
<%-- 基础地址已含查询串时用 & 连接，否则用 ? 开头，避免出现 /path&pageNum=1 这种非法 URL --%>
<c:set var="pagerSep" value="${fn:contains(baseUrl, '?') ? '&' : '?'}" scope="page"/>
<c:if test="${pageTotal > 1}">
    <div class="pager">
        <span class="info">第 ${pageNum} / ${pageTotal} 页，共 ${total} 条</span>
        <span class="pager__list">
            <c:choose>
                <c:when test="${pageNum > 1}">
                    <a class="pager__item" href="${baseUrl}${pagerSep}pageNum=${pageNum - 1}${pagerQs}">上一页</a>
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
                        <a class="pager__item" href="${baseUrl}${pagerSep}pageNum=${i}${pagerQs}">${i}</a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <c:choose>
                <c:when test="${pageNum < pageTotal}">
                    <a class="pager__item" href="${baseUrl}${pagerSep}pageNum=${pageNum + 1}${pagerQs}">下一页</a>
                </c:when>
                <c:otherwise>
                    <span class="pager__item is-disabled">下一页</span>
                </c:otherwise>
            </c:choose>
        </span>
    </div>
</c:if>
