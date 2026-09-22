<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>오류 | 가계부</title>
    <link rel="stylesheet" href="<c:url value='/resources/css/reset.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
    <link rel="stylesheet" href="<c:url value='/resources/css/login.css'/>">
</head>
<body class="login-body">
<main class="login-page">
    <h1>오류 <c:out value="${pageContext.errorData.statusCode}"/></h1>
    <p>요청을 처리할 수 없습니다.</p>
    <p class="login-links"><a href="<c:url value='/'/>">처음으로</a></p>
</main>
</body>
</html>
