<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 | 가계부</title>
    <link rel="manifest" href="<c:url value='/manifest.json'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/reset.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/login.css'/>">
    <script>var contextPath = "${pageContext.request.contextPath}";</script>
    <script defer src="<c:url value='/resources/js/lib/jquery.1.11.3.min.js'/>"></script>
    <script defer src="<c:url value='/resources/js/common/common.js'/>"></script>
    <script defer src="<c:url value='/resources/js/common/modal.js'/>"></script>
    <script defer src="<c:url value='/resources/js/app/login/login.js'/>"></script>
</head>
<body class="login-body">
<main class="login-page">
    <h1>가계부 로그인</h1>
    <form id="loginForm" class="login-form" novalidate>
        <label class="login-field" for="username">
            <span>아이디</span>
            <input id="username" name="username" type="text" autocomplete="username" autofocus required>
        </label>
        <label class="login-field" for="password">
            <span>비밀번호</span>
            <input id="password" name="password" type="password" autocomplete="current-password" required>
        </label>
        <button class="login-submit-button" type="submit">로그인</button>
    </form>
    <p class="login-links"><a href="<c:url value='/signup'/>">회원가입</a></p>
</main>
</body>
</html>
