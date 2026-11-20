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
    <link rel="apple-touch-icon" href="<c:url value='/resources/images/icon-180.png'/>">
    <meta name="theme-color" content="#f3f8fa">
    <link rel="icon" href="<c:url value='/resources/images/ledger-leaf.svg'/>" type="image/svg+xml">
    <link rel="stylesheet" href="<c:url value='/resources/css/reset.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/style.css'/>">
    <script>var contextPath = "${pageContext.request.contextPath}";</script>
    <script src="<c:url value='/resources/js/lib/chartjs/chart.umd.js'/>"></script>
    <script src="<c:url value='/resources/js/common/common.js'/>"></script>
    <script src="<c:url value='/resources/js/common/modal.js'/>"></script>
    <script src="<c:url value='/resources/js/common/leaf.js'/>"></script>
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
        <div class="page-content">
            <%-- 돈 관리: 자산 · 통장 · 고정 항목을 한 메뉴 안의 탭으로 --%>
            <c:if test="${page eq 'asset' or page eq 'account' or page eq 'recurring'}">
                <nav class="sub-tabs" aria-label="돈 관리">
                    <a href="<c:url value='/ledger/asset'/>" aria-current="${page eq 'asset' ? 'page' : 'false'}"><svg class="icon" aria-hidden="true"><use href="#i-leaf"></use></svg>자산</a>
                    <a href="<c:url value='/ledger/account'/>" aria-current="${page eq 'account' ? 'page' : 'false'}"><svg class="icon" aria-hidden="true"><use href="#i-bank"></use></svg>통장</a>
                    <a href="<c:url value='/ledger/recurring'/>" aria-current="${page eq 'recurring' ? 'page' : 'false'}"><svg class="icon" aria-hidden="true"><use href="#i-repeat"></use></svg>고정 항목</a>
                </nav>
            </c:if>
            <jsp:doBody/>
        </div>
    </main>
</div>
</body>
</html>
