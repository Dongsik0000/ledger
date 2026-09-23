<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="theme-color" content="#45654c">
<title>오류 안내 · 나의 가계부</title>
<link rel="icon" href="<c:url value='/resources/images/ledger-leaf.svg'/>">
<link rel="manifest" href="<c:url value='/manifest.json'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/reset.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/style.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/login.css'/>">
</head>
<body class="error-body">
<jsp:include page="/WEB-INF/layout/icons.jsp"/>
<main class="panel error-page reveal">
<div class="brand">
<span class="brand-mark">
<svg class="icon" aria-hidden="true">
<use href="#i-leaf">
</use>
</svg>
</span>
<span>
<span class="brand-name">가계부</span>
<span class="brand-caption">LITTLE BY LITTLE</span>
</span>
</div>
<div class="error-code">
<c:out value="${pageContext.errorData.statusCode}" default="오류"/>
</div>
<h1>잠시, 길을 벗어났어요</h1>
<p>페이지를 찾을 수 없거나 요청을 처리하지 못했어요.<br>처음으로 돌아가 다시 시도해 주세요.</p>
<a class="button primary" href='<c:url value="/"/>'>처음으로 돌아가기<svg class="icon" aria-hidden="true">
<use href="#i-right">
</use>
</svg>
</a>
</main>
</body>
</html>
