<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 정적 UI 예시. 신규 이벤트·계산·저장 로직 없음. 금액과 날짜는 디자인 확인용. --%>
<jsp:useBean id="uiToday" class="java.util.Date"/>
<fmt:formatDate value="${uiToday}" pattern="yyyy-MM-dd" var="uiDate"/>
<t:layout title="대시보드" page="dashboard">
<jsp:attribute name="script">
<script defer src="<c:url value='/resources/js/app/dashboard/dashboard.js'/>"></script>
</jsp:attribute>
<jsp:body>
<header class="page-head">
<div>
<p class="eyebrow">MY DAILY NOTE</p>
<h1>오늘의 가계부</h1>
<p class="page-description">오늘의 작은 소비부터 내일의 여유까지.</p>
</div>
<a class="button primary" href="#quick-entry">바로 기록</a>
</header>
<div class="cycle-strip">
<span>
<svg class="icon" aria-hidden="true">
<use href="#i-calendar">
</use>
</svg>현재 주기 <strong>2026.09.26 — 10.25</strong>
</span>
<span class="chip">18일 남음</span>
</div>
<div class="budget-grid">
<section class="panel budget-hero reveal">
<div class="metric-label">오늘 쓸 수 있는 돈<svg class="icon" aria-hidden="true">
<use href="#i-sun">
</use>
</svg>
</div>
<p class="daily-budget">95,000<span>원</span>
</p>
<p>예정된 고정지출을 남겨두고, 오늘의 여유를 확인해요.</p>
<div class="budget-foot">
<span>예정 고정지출 <strong>220,000원</strong>
</span>
<span>기준일 2026.10.08</span>
</div>
</section>
<section class="panel cycle-summary reveal">
<div class="panel-head">
<div>
<h2>이번 주기의 기록</h2>
<p>급여일부터 다음 급여일 전날까지</p>
</div>
</div>
<dl class="summary-lines">
<div>
<dt>이월 금액</dt>
<dd>480,000원</dd>
</div>
<div>
<dt>주기 수입</dt>
<dd class="income">+3,200,000원</dd>
</div>
<div>
<dt>주기 지출</dt>
<dd class="expense">−1,750,000원</dd>
</div>
<div class="summary-total">
<dt>주기 잔액</dt>
<dd>1,930,000원</dd>
</div>
</dl>
</section>
</div>
<div class="dashboard-grid">
<section class="panel quick-entry reveal" id="quick-entry" tabindex="-1">
<div class="panel-head">
<div>
<h2>빠른 거래 입력</h2>
<p>잊기 전에, 오늘의 기록을 남겨요.</p>
</div>
<svg class="icon" aria-hidden="true">
<use href="#i-edit">
</use>
</svg>
</div>
<fieldset class="form-stack">
<legend class="sr-only">거래 입력</legend>
<label class="field">
<span>금액</span>
<input type="text" name="quick-amount" inputmode="numeric" pattern="[0-9,]+" autocomplete="off" placeholder="0" class="amount-input" aria-describedby="quick-amount-help">
</label>
<p id="quick-amount-help" class="form-note">원 · 소수점 없이 입력해 주세요.</p>
<fieldset class="choice-field">
<legend>구분</legend>
<div class="choice-buttons">
<label>
<input type="radio" name="quick-type" value="EXPENSE" checked>
<span>지출</span>
</label>
<label>
<input type="radio" name="quick-type" value="INCOME">
<span>수입</span>
</label>
</div>
</fieldset>
<fieldset class="choice-field">
<legend>카테고리</legend>
<div class="choice-buttons">
<label>
<input type="radio" name="quick-category" value="식비" checked>
<span>식비</span>
</label>
<label>
<input type="radio" name="quick-category" value="배달">
<span>배달</span>
</label>
<label>
<input type="radio" name="quick-category" value="카페/간식">
<span>카페/간식</span>
</label>
<label>
<input type="radio" name="quick-category" value="장보기">
<span>장보기</span>
</label>
<label>
<input type="radio" name="quick-category" value="교통">
<span>교통</span>
</label>
<label>
<input type="radio" name="quick-category" value="고정지출">
<span>고정지출</span>
</label>
<label>
<input type="radio" name="quick-category" value="저축">
<span>저축</span>
</label>
<label>
<input type="radio" name="quick-category" value="기타">
<span>기타</span>
</label>
<label>
<input type="radio" name="quick-category" value="급여">
<span>급여</span>
</label>
</div>
</fieldset>
<label class="field">
<span>내용</span>
<input type="text" name="quick-title" placeholder="어디에 사용하셨나요?" maxlength="100">
</label>
<fieldset class="choice-field">
<legend>결제수단</legend>
<div class="choice-buttons">
<label>
<input type="radio" name="quick-payment" value="신용카드">
<span>신용카드</span>
</label>
<label>
<input type="radio" name="quick-payment" value="체크카드" checked>
<span>체크카드</span>
</label>
<label>
<input type="radio" name="quick-payment" value="계좌이체">
<span>계좌이체</span>
</label>
<label>
<input type="radio" name="quick-payment" value="현금">
<span>현금</span>
</label>
</div>
</fieldset>
<details class="optional-fields"><summary>날짜 · 메모 <span>기본 오늘</span></summary><div class="field-row">
<label class="field">
<span>날짜</span>
<input type="date" name="quick-date" value="${uiDate}" >
</label>
<label class="field">
<span>메모 (선택)</span>
<input type="text" name="quick-memo" placeholder="기억하고 싶은 한 줄" maxlength="500">
</label>
</div></details>
<button type="button" class="button primary full-button" >
<svg class="icon" aria-hidden="true">
<use href="#i-plus">
</use>
</svg>기록하기</button>
</fieldset>
</section>
<section class="panel recent-panel reveal">
<div class="panel-head">
<div>
<h2>최근의 기록</h2>
<p>가장 최근 거래 10건</p>
</div>
<a class="text-link" href="<c:url value='/ledger/entry'/>">전체 보기<svg class="icon" aria-hidden="true">
<use href="#i-right">
</use>
</svg>
</a>
</div>
<div class="date-label">
<time datetime="2026-10-08">10월 8일</time>
<span>일 합계 <strong class="expense">−17,300원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon warm">
<svg class="icon" aria-hidden="true">
<use href="#i-coffee">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>동네 카페</strong>
<small>카페/간식 · 체크카드</small>
<span class="memo-tag" title="따뜻한 라테">
<svg class="icon" aria-hidden="true">
<use href="#i-edit">
</use>
</svg>
<span>따뜻한 라테</span>
</span>
</div>
<strong class="amount expense">−4,800원</strong>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-food">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>오늘의 점심</strong>
<small>식비 · 신용카드</small>
</div>
<strong class="amount expense">−12,500원</strong>
</div>
<div class="date-label">
<time datetime="2026-10-07">10월 7일</time>
<span>일 합계 <strong class="expense">−46,700원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-bag">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>주말 장보기</strong>
<small>장보기 · 체크카드</small>
<span class="memo-tag" title="과일과 채소">
<svg class="icon" aria-hidden="true">
<use href="#i-edit">
</use>
</svg>
<span>과일과 채소</span>
</span>
</div>
<strong class="amount expense">−45,200원</strong>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-train">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>지하철</strong>
<small>교통 · 체크카드</small>
</div>
<strong class="amount expense">−1,500원</strong>
</div>
<div class="date-label">
<time datetime="2026-10-06">10월 6일</time>
<span>일 합계 <strong class="expense">−218,000원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-book">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>읽고 싶었던 책</strong>
<small>기타 · 신용카드</small>
</div>
<strong class="amount expense">−18,000원</strong>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-leaf">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>차곡차곡 적금</strong>
<small>저축 · 계좌이체</small>
<span class="memo-tag" title="10월 저축">
<svg class="icon" aria-hidden="true">
<use href="#i-edit">
</use>
</svg>
<span>10월 저축</span>
</span>
</div>
<strong class="amount expense">−200,000원</strong>
</div>
<div class="date-label">
<time datetime="2026-10-05">10월 5일</time>
<span>일 합계 <strong class="expense">−582,000원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-food">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>친구와 저녁</strong>
<small>식비 · 신용카드</small>
</div>
<strong class="amount expense">−32,000원</strong>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-home">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>월세</strong>
<small>고정지출 · 계좌이체</small>
</div>
<strong class="amount expense">−550,000원</strong>
</div>
<div class="date-label">
<time datetime="2026-10-03">10월 3일</time>
<span>일 합계 <strong class="expense">−4,500원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon warm">
<svg class="icon" aria-hidden="true">
<use href="#i-coffee">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>오후의 커피</strong>
<small>카페/간식 · 현금</small>
</div>
<strong class="amount expense">−4,500원</strong>
</div>
<div class="date-label">
<time datetime="2026-10-01">10월 1일</time>
<span>일 합계 <strong class="income">+3,200,000원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-wallet">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>월급</strong>
<small>급여 · 계좌이체</small>
</div>
<strong class="amount income">+3,200,000원</strong>
</div>
</section>
</div>
<section class="total-strip">
<span>
<svg class="icon" aria-hidden="true">
<use href="#i-wallet">
</use>
</svg>나의 총 잔액</span>
<strong>8,980,000원</strong>
<small>가계부 1,930,000원 + 자산 7,050,000원</small>
</section>
<details class="formula-note">
<summary>오늘 쓸 수 있는 돈은 어떻게 계산하나요?</summary>
<p>(이월 480,000원 + 수입 3,200,000원 − 지출 1,750,000원 − 예정 고정지출 220,000원) ÷ 남은 18일 = 95,000원</p>
<p>달력의 월과 관계없이 오늘이 속한 주기를 기준으로 표시합니다.</p>
</details>
</jsp:body>
</t:layout>
