<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<aside class="app-sidebar" aria-label="주 메뉴">
<a class="brand" href="<c:url value='/ledger/dashboard'/>">
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
</a>
<p class="nav-label">MY MONEY NOTE</p>
<nav class="app-nav" aria-label="가계부 메뉴">
<a class="nav-item${requestScope.uiPage eq 'dashboard' ? ' is-active' : ''}" aria-current="${requestScope.uiPage eq 'dashboard' ? 'page' : 'false'}" href="<c:url value='/ledger/dashboard'/>">
<svg class="icon" aria-hidden="true">
<use href="#i-home">
</use>
</svg>
<span>대시보드</span>
</a>
<a class="nav-item${requestScope.uiPage eq 'entry' ? ' is-active' : ''}" aria-current="${requestScope.uiPage eq 'entry' ? 'page' : 'false'}" href="<c:url value='/ledger/entry'/>">
<svg class="icon" aria-hidden="true">
<use href="#i-book">
</use>
</svg>
<span>거래 내역</span>
</a>
<a class="nav-item${requestScope.uiPage eq 'summary' ? ' is-active' : ''}" aria-current="${requestScope.uiPage eq 'summary' ? 'page' : 'false'}" href="<c:url value='/ledger/summary'/>">
<svg class="icon" aria-hidden="true">
<use href="#i-chart">
</use>
</svg>
<span>요약</span>
</a>
<a class="nav-item${requestScope.uiPage eq 'recurring' ? ' is-active' : ''}" aria-current="${requestScope.uiPage eq 'recurring' ? 'page' : 'false'}" href="<c:url value='/ledger/recurring'/>">
<svg class="icon" aria-hidden="true">
<use href="#i-repeat">
</use>
</svg>
<span>고정 항목</span>
</a>
<a class="nav-item${requestScope.uiPage eq 'asset' ? ' is-active' : ''}" aria-current="${requestScope.uiPage eq 'asset' ? 'page' : 'false'}" href="<c:url value='/ledger/asset'/>">
<svg class="icon" aria-hidden="true">
<use href="#i-wallet">
</use>
</svg>
<span>자산</span>
</a>
<a class="nav-item${requestScope.uiPage eq 'settings' ? ' is-active' : ''}" aria-current="${requestScope.uiPage eq 'settings' ? 'page' : 'false'}" href="<c:url value='/ledger/settings'/>">
<svg class="icon" aria-hidden="true">
<use href="#i-settings">
</use>
</svg>
<span>설정</span>
</a>
</nav>
<div class="sidebar-bottom">
<p class="sidebar-note">작은 기록이 모여,<br>더 나은 내일로.<span>오늘도 나를 위한 한 줄.</span>
</p>
<div class="profile">
<span class="avatar">
<svg class="icon" aria-hidden="true">
<use href="#i-user">
</use>
</svg>
</span>
<div class="profile-copy">
<strong>
<c:out value="${sessionScope.username}" default="나의 기록"/>
</strong>
<small>나만의 가계부</small>
</div>
<a class="icon-button" href="<c:url value='/logout'/>">
<svg class="icon" aria-hidden="true">
<use href="#i-logout">
</use>
</svg>
<span class="sr-only">로그아웃</span>
</a>
</div>
</div>
</aside>
