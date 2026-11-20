<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%-- 메뉴 4개(오늘 · 기록 · 흐름 · 돈 관리). 설정·로그아웃은 프로필 메뉴. 휴대폰은 아래 탭(가운데 + 는 기록 추가) --%>
<c:set var="p" value="${requestScope.uiPage}"/>
<c:set var="money" value="${p eq 'asset' or p eq 'account' or p eq 'recurring'}"/>
<aside class="app-sidebar" aria-label="주 메뉴">
    <a class="brand" href="<c:url value='/ledger/dashboard'/>">
        <span class="brand-mark" data-leaf="happy" aria-hidden="true"></span>
        <span>
            <span class="brand-name">가계부</span>
            <span class="brand-caption">작은 기록이 모여 한 달의 흐름</span>
        </span>
    </a>
    <nav class="app-nav" aria-label="가계부 메뉴">
        <a class="nav-item${p eq 'dashboard' ? ' is-active' : ''}" aria-current="${p eq 'dashboard' ? 'page' : 'false'}" href="<c:url value='/ledger/dashboard'/>">
            <svg class="icon" aria-hidden="true"><use href="#i-sun"></use></svg><span>오늘</span>
        </a>
        <a class="nav-item${p eq 'entry' ? ' is-active' : ''}" aria-current="${p eq 'entry' ? 'page' : 'false'}" href="<c:url value='/ledger/entry'/>">
            <svg class="icon" aria-hidden="true"><use href="#i-calendar"></use></svg><span>기록</span>
        </a>
        <a class="nav-item${p eq 'summary' ? ' is-active' : ''}" aria-current="${p eq 'summary' ? 'page' : 'false'}" href="<c:url value='/ledger/summary'/>">
            <svg class="icon" aria-hidden="true"><use href="#i-chart"></use></svg><span>흐름</span>
        </a>
        <a class="nav-item${money ? ' is-active' : ''}" aria-current="${money ? 'page' : 'false'}" href="<c:url value='/ledger/asset'/>">
            <svg class="icon" aria-hidden="true"><use href="#i-wallet"></use></svg><span>돈 관리</span>
        </a>
    </nav>
    <div class="sidebar-bottom">
        <details class="profile-menu">
            <summary class="profile">
                <span class="avatar"><svg class="icon" aria-hidden="true"><use href="#i-user"></use></svg></span>
                <span class="profile-copy">
                    <strong><c:out value="${sessionScope.username}" default="나의 기록"/></strong>
                    <small>설정 · 로그아웃</small>
                </span>
                <span class="sr-only">내 메뉴 열기</span>
            </summary>
            <div class="profile-pop">
                <p>설정</p>
                <a href="<c:url value='/ledger/settings#cycle-settings'/>"><svg class="icon" aria-hidden="true"><use href="#i-calendar"></use></svg>기록 주기 · 시작 잔액</a>
                <a href="<c:url value='/ledger/settings#list-settings'/>"><svg class="icon" aria-hidden="true"><use href="#i-settings"></use></svg>카테고리 · 결제수단 · 예산</a>
                <a href="<c:url value='/ledger/settings#export-settings'/>"><svg class="icon" aria-hidden="true"><use href="#i-download"></use></svg>기록 내보내기</a>
                <a href="<c:url value='/ledger/settings#account-settings'/>"><svg class="icon" aria-hidden="true"><use href="#i-shield"></use></svg>비밀번호 · 초기화 · 탈퇴</a>
                <hr>
                <button type="button" data-logout><svg class="icon" aria-hidden="true"><use href="#i-logout"></use></svg>로그아웃</button>
            </div>
        </details>
    </div>
</aside>
<nav class="app-tabbar" aria-label="하단 메뉴">
    <a class="tab-item${p eq 'dashboard' ? ' is-active' : ''}" aria-current="${p eq 'dashboard' ? 'page' : 'false'}" href="<c:url value='/ledger/dashboard'/>">
        <svg class="icon" aria-hidden="true"><use href="#i-sun"></use></svg>오늘
    </a>
    <a class="tab-item${p eq 'entry' ? ' is-active' : ''}" aria-current="${p eq 'entry' ? 'page' : 'false'}" href="<c:url value='/ledger/entry'/>">
        <svg class="icon" aria-hidden="true"><use href="#i-calendar"></use></svg>기록
    </a>
    <%-- 오늘·기록 화면에서는 그 화면의 입력 창을 바로 열고(data-quick-add), 다른 화면에서는 기록 화면의 추가 창으로 간다 --%>
    <a class="tab-add" href="<c:url value='/ledger/entry?new=1'/>" data-quick-add aria-label="기록하기">
        <svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>
    </a>
    <a class="tab-item${p eq 'summary' ? ' is-active' : ''}" aria-current="${p eq 'summary' ? 'page' : 'false'}" href="<c:url value='/ledger/summary'/>">
        <svg class="icon" aria-hidden="true"><use href="#i-chart"></use></svg>흐름
    </a>
    <a class="tab-item${money ? ' is-active' : ''}" aria-current="${money ? 'page' : 'false'}" href="<c:url value='/ledger/asset'/>">
        <svg class="icon" aria-hidden="true"><use href="#i-wallet"></use></svg>돈 관리
    </a>
</nav>
