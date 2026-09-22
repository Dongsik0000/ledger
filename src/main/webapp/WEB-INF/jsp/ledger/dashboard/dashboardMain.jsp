<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<t:layout title="대시보드">
    <jsp:attribute name="script">
        <script defer src="<c:url value='/resources/js/app/dashboard/dashboard.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <h1>대시보드</h1>
        <p>로그인 사용자: <c:out value="${sessionScope.username}"/></p>
        <%-- 주기 요약 카드, 빠른 입력, 최근 거래는 여기에 --%>
    </jsp:body>
</t:layout>
