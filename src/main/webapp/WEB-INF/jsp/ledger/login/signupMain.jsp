<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입 | 가계부</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/reset.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/login.css'/>">
    <script>var contextPath = "${pageContext.request.contextPath}";</script>
    <script defer src="<c:url value='/resources/js/lib/jquery.1.11.3.min.js'/>"></script>
    <script defer src="<c:url value='/resources/js/common/common.js'/>"></script>
    <script defer src="<c:url value='/resources/js/common/modal.js'/>"></script>
    <script defer src="<c:url value='/resources/js/app/login/signup.js'/>"></script>
</head>
<body class="login-body">
<main class="login-page">
    <h1>회원가입</h1>
    <form id="signupForm" class="login-form" novalidate>
        <label class="login-field" for="username">
            <span>아이디</span>
            <input id="username" name="username" type="text" autocomplete="username" autofocus required>
        </label>
        <label class="login-field" for="password">
            <span>비밀번호 (8자 이상)</span>
            <input id="password" name="password" type="password" autocomplete="new-password" minlength="8" required>
        </label>
        <label class="login-field" for="signupCode">
            <span>가입 코드</span>
            <input id="signupCode" name="signupCode" type="text" autocomplete="off" required>
        </label>
        <button class="login-submit-button" type="submit">가입</button>
    </form>
    <p class="login-links"><a href="<c:url value='/login'/>">로그인으로</a></p>
</main>
</body>
</html>
