<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<%-- 현재 요청 경로로 활성 메뉴 표시 --%>
<c:set var="uri" value="${pageContext.request.requestURI}"/>
<aside class="app-sidebar" aria-label="메뉴">
    <a class="nav-item${fn:contains(uri, '/dashboard') ? ' is-active' : ''}" href="<c:url value='/ledger/dashboard'/>">대시보드</a>
    <a class="nav-item${fn:contains(uri, '/entry') ? ' is-active' : ''}" href="<c:url value='/ledger/entry'/>">거래 내역</a>
    <a class="nav-item${fn:contains(uri, '/summary') ? ' is-active' : ''}" href="<c:url value='/ledger/summary'/>">요약</a>
    <a class="nav-item${fn:contains(uri, '/recurring') ? ' is-active' : ''}" href="<c:url value='/ledger/recurring'/>">고정 항목</a>
    <a class="nav-item${fn:contains(uri, '/asset') ? ' is-active' : ''}" href="<c:url value='/ledger/asset'/>">자산</a>
    <a class="nav-item${fn:contains(uri, '/settings') ? ' is-active' : ''}" href="<c:url value='/ledger/settings'/>">설정</a>
    <a class="nav-item" href="<c:url value='/logout'/>">로그아웃 (${sessionScope.username})</a>
</aside>
