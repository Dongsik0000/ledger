<%@ tag language="java" pageEncoding="UTF-8" body-content="scriptless"%>
<%@ attribute name="title" required="false"%>
<%@ attribute name="script" required="false" fragment="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${empty title ? '가계부' : title}"/> | 가계부</title>
    <link rel="manifest" href="<c:url value='/manifest.json'/>">
    <meta name="theme-color" content="#7C6FE0">
    <link rel="icon" href="<c:url value='/resources/images/icon.svg'/>" type="image/svg+xml">
    <link rel="stylesheet" href="<c:url value='/resources/css/reset.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/style.css'/>">
    <script>var contextPath = "${pageContext.request.contextPath}";</script>
    <script src="<c:url value='/resources/js/lib/jquery.1.11.3.min.js'/>"></script>
    <script src="<c:url value='/resources/js/lib/chartjs/chart.umd.js'/>"></script>
    <script src="<c:url value='/resources/js/common/common.js'/>"></script>
    <script src="<c:url value='/resources/js/common/modal.js'/>"></script>
    <%-- 화면별 JS: <jsp:attribute name="script"><script src=...></script></jsp:attribute> --%>
    <jsp:invoke fragment="script"/>
</head>
<body>
<div class="app-shell">
    <jsp:include page="/WEB-INF/layout/ledgerMenu.jsp"/>
    <main class="app-main">
        <jsp:doBody/>
    </main>
</div>
</body>
</html>
