<%@ tag language="java" pageEncoding="UTF-8" body-content="scriptless"%>
<%@ attribute name="title" required="false"%>
<%@ attribute name="page" required="false"%>
<%@ attribute name="script" required="false" fragment="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="uiPage" value="${page}" scope="request"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:if test="${not empty title}"><c:out value="${title}"/> · </c:if>나의 가계부</title>
    <link rel="manifest" href="<c:url value='/manifest.json'/>">
    <meta name="theme-color" content="#45654c">
    <link rel="icon" href="<c:url value='/resources/images/ledger-leaf.svg'/>" type="image/svg+xml">
    <link rel="stylesheet" href="<c:url value='/resources/css/reset.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/style.css'/>">
    <script>var contextPath = "${pageContext.request.contextPath}";</script>
    <script src="<c:url value='/resources/js/lib/jquery.1.11.3.min.js'/>"></script>
    <script src="<c:url value='/resources/js/lib/chartjs/chart.umd.js'/>"></script>
    <script src="<c:url value='/resources/js/common/common.js'/>"></script>
    <script src="<c:url value='/resources/js/common/modal.js'/>"></script>
    <%-- 화면별 JS: <jsp:attribute name="script">
<script src=...></script>
</jsp:attribute> --%>
    <jsp:invoke fragment="script"/>
</head>
<body>
<jsp:include page="/WEB-INF/layout/icons.jsp"/>
<a class="skip-link" href="#main-content">본문으로 바로가기</a>
<div class="app-shell">
    <jsp:include page="/WEB-INF/layout/ledgerMenu.jsp"/>
    <main class="app-main" id="main-content" tabindex="-1">
<div class="app-topbar">
<div class="breadcrumb">
<span>나의 기록</span>
<span aria-hidden="true">/</span>
<strong>
<c:out value="${title}"/>
</strong>
</div>
</div>
<div class="page-content">
        <jsp:doBody/>
    <footer class="subtle-footer">
<span>오늘의 기록, 내일의 여유.</span>
<span>LITTLE BY LITTLE</span>
</footer>
</div>
</main>
</div>
</body>
</html>
