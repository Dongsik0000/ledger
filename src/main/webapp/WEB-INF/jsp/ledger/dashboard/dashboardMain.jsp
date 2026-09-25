<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 대시보드: 값은 dashboard.js 가 /ledger/dashboard/summary·recent, 선택지는 /ledger/settings/master 로 채운다 --%>
<t:layout title="대시보드" page="dashboard">
<jsp:attribute name="script">
<script defer src="<c:url value='/resources/js/app/entry/entryForm.js'/>"></script>
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
<svg class="icon" aria-hidden="true"><use href="#i-calendar"></use></svg>현재 주기 <strong id="cycleRange">—</strong>
</span>
<span class="chip" id="daysLeft">—</span>
</div>
<div class="budget-grid">
<section class="panel budget-hero reveal">
<div class="metric-label">오늘 쓸 수 있는 돈<svg class="icon" aria-hidden="true"><use href="#i-sun"></use></svg>
</div>
<p class="daily-budget"><span id="dailyBudget">—</span><span>원</span></p>
<p>예정된 고정지출을 남겨두고, 오늘의 여유를 확인해요.</p>
<div class="budget-foot">
<span>예정 고정지출 <strong id="pendingFixed">—</strong></span>
<span id="todayLabel">기준일 —</span>
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
<div><dt>이월 금액</dt><dd id="carryOver">—</dd></div>
<div><dt>주기 수입</dt><dd class="income" id="cycleIncome">—</dd></div>
<div><dt>주기 지출</dt><dd class="expense" id="cycleExpense">—</dd></div>
<div class="summary-total"><dt>주기 잔액</dt><dd id="cycleBalance">—</dd></div>
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
<svg class="icon" aria-hidden="true"><use href="#i-edit"></use></svg>
</div>
<fieldset class="form-stack">
<legend class="sr-only">거래 입력</legend>
<label class="field">
<span>금액</span>
<input type="text" id="quickAmount" inputmode="numeric" autocomplete="off" placeholder="0" class="amount-input" aria-describedby="quick-amount-help">
</label>
<p id="quick-amount-help" class="form-note">원 · 소수점 없이 입력해 주세요.</p>
<fieldset class="choice-field">
<legend>구분</legend>
<div class="choice-buttons">
<label><input type="radio" name="quickType" value="EXPENSE" checked><span>지출</span></label>
<label><input type="radio" name="quickType" value="INCOME"><span>수입</span></label>
</div>
</fieldset>
<fieldset class="choice-field">
<legend>카테고리</legend>
<div class="choice-buttons" id="quickCategories"></div>
</fieldset>
<label class="field">
<span>내용</span>
<input type="text" id="quickTitle" placeholder="어디에 사용하셨나요?" maxlength="100">
</label>
<fieldset class="choice-field">
<legend>결제수단</legend>
<div class="choice-buttons" id="quickPayments"></div>
</fieldset>
<details class="optional-fields" id="quickOptional"><summary>날짜 · 메모 <span>기본 오늘</span></summary><div class="field-row">
<label class="field">
<span>날짜</span>
<input type="date" id="quickDate">
</label>
<label class="field">
<span>메모 (선택)</span>
<input type="text" id="quickMemo" placeholder="기억하고 싶은 한 줄" maxlength="500">
</label>
</div></details>
<button type="button" class="button primary full-button" id="quickSave">
<svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>기록하기</button>
</fieldset>
</section>
<section class="panel recent-panel reveal">
<div class="panel-head">
<div>
<h2>최근의 기록</h2>
<p>가장 최근 거래 10건</p>
</div>
<a class="text-link" href="<c:url value='/ledger/entry'/>">전체 보기<svg class="icon" aria-hidden="true"><use href="#i-right"></use></svg>
</a>
</div>
<div id="recentList"></div>
</section>
</div>
<section class="total-strip">
<span>
<svg class="icon" aria-hidden="true"><use href="#i-wallet"></use></svg>나의 총 잔액</span>
<strong id="totalBalance">—</strong>
<small id="totalDetail">가계부 — + 자산 —</small>
</section>
<details class="formula-note">
<summary>오늘 쓸 수 있는 돈은 어떻게 계산하나요?</summary>
<p id="formula">—</p>
<p>달력의 월과 관계없이 오늘이 속한 주기를 기준으로 표시합니다.</p>
</details>
</jsp:body>
</t:layout>
