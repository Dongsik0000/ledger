<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="theme-color" content="#45654c">
<title>로그인 · 나의 가계부</title>
<link rel="icon" href="<c:url value='/resources/images/ledger-leaf.svg'/>">
<link rel="manifest" href="<c:url value='/manifest.json'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/reset.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/common.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/style.css'/>">
<link rel="stylesheet" href="<c:url value='/resources/css/login.css'/>">
<script>var contextPath = "${pageContext.request.contextPath}";</script>
<script defer src="<c:url value='/resources/js/common/common.js'/>"></script>
<script defer src="<c:url value='/resources/js/common/modal.js'/>"></script>
<script defer src="<c:url value='/resources/js/app/login/login.js'/>"></script>
</head>
<body class="login-body">
<jsp:include page="/WEB-INF/layout/icons.jsp"/>
<div class="auth-shell">
<aside class="auth-story">
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
<div class="auth-story-copy">
<p class="eyebrow">A LITTLE NOTE FOR YOUR DAY</p>
<h2>작은 기록이 모여,<br>더 나은 내일로.</h2>
<p>오늘의 커피 한 잔부터 내일을 위한 저축까지.<br>나의 일상을 담는 편안한 가계부.</p>
<div class="auth-art" aria-hidden="true">
<div class="paper-note">
<svg class="icon" aria-hidden="true">
<use href="#i-leaf">
</use>
</svg>
<span>
</span>
<span>
</span>
<span>
</span>
</div>
<div class="art-leaf">
</div>
</div>
</div>
<footer class="auth-story-footer">LITTLE BY LITTLE, DAY BY DAY.</footer>
</aside>
<main class="auth-main">
<div class="login-page reveal">
<div class="brand mobile-brand">
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
<p class="eyebrow">WELCOME BACK</p>
<h1>다시 만나 반가워요</h1>
<p class="auth-intro">오늘의 이야기를 가계부에 남겨볼까요?</p>
<div class="inline-error" role="alert" hidden>아이디 또는 비밀번호를 확인해 주세요.</div>
<form id="loginForm" class="login-form" novalidate>
<label class="login-field" for="username">
<span>아이디</span>
<input id="username" name="username" type="text" placeholder="아이디를 입력해 주세요" autocomplete="username" required>
</label>
<label class="login-field" for="password">
<span>비밀번호</span>
<input id="password" name="password" type="password" placeholder="비밀번호를 입력해 주세요" autocomplete="current-password" required>
</label>
<button class="login-submit-button" type="submit">로그인</button>
</form>
<p class="login-links">처음 오셨나요? <a class="" href='<c:url value="/signup"/>'>회원가입</a>
</p>
<p class="auth-fineprint">
<svg class="icon" aria-hidden="true">
<use href="#i-shield">
</use>
</svg>나의 기록은 나의 계정에서.<br>매일 조금씩, 나만의 속도로 기록하세요.</p>
</div>
</main>
</div>
</body>
</html>
