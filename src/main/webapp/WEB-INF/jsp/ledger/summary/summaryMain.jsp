<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 요약: 표·차트는 summary.js 가 /ledger/summary/load·donut, 주기 지표는 /ledger/dashboard/summary 로 채운다 --%>
<t:layout title="요약" page="summary">
<jsp:attribute name="script">
<script defer src="<c:url value='/resources/js/app/summary/summary.js'/>"></script>
</jsp:attribute>
<jsp:body>
<header class="page-head">
<div>
<p class="eyebrow">MONEY IN PERSPECTIVE</p>
<h1>요약 / 통계</h1>
<p class="page-description">기록을 모아, 나에게 맞는 소비의 흐름을 찾아요.</p>
</div>
</header>
<section class="panel range-panel">
<div class="range-fields">
<label class="field">
<span>기준</span>
<select id="basis">
<option value="MONTH">달력 월</option>
<option value="CYCLE">주기</option>
</select>
</label>
<label class="field">
<span>시작 월</span>
<input type="month" id="rangeFrom">
</label>
<span aria-hidden="true">—</span>
<label class="field">
<span>종료 월</span>
<input type="month" id="rangeTo">
</label>
<button type="button" class="button primary" id="rangeSearch">조회</button>
</div>
<p class="form-note" id="basisNote">아래 월별 표·차트는 달력 월 기준입니다(최대 24개월). 현재 주기 잔액은 별도로 표시합니다.</p>
</section>
<div class="cycle-strip">
<span>
<svg class="icon" aria-hidden="true"><use href="#i-calendar"></use></svg>현재 주기 <strong id="cycleRange">—</strong>
</span>
<span class="chip" id="daysLeft">—</span>
</div>
<div class="three-metrics">
<article class="panel metric featured">
<div class="metric-label">현재 주기 잔액<svg class="icon" aria-hidden="true"><use href="#i-wallet"></use></svg></div>
<p class="metric-value"><span id="cycleBalance">—</span><small>원</small></p>
<p class="metric-foot" id="cycleBalanceFoot">이월 포함</p>
</article>
<article class="panel metric">
<div class="metric-label">현재 주기 수입<svg class="icon" aria-hidden="true"><use href="#i-chart"></use></svg></div>
<p class="metric-value"><span id="cycleIncome">—</span><small>원</small></p>
<p class="metric-foot" id="cycleIncomeFoot">—</p>
</article>
<article class="panel metric sage">
<div class="metric-label">현재 주기 지출<svg class="icon" aria-hidden="true"><use href="#i-book"></use></svg></div>
<p class="metric-value"><span id="cycleExpense">—</span><small>원</small></p>
<p class="metric-foot" id="cycleExpenseFoot">—</p>
</article>
</div>
<section class="panel">
<div class="panel-head">
<div>
<h2>월별 요약</h2>
<p>가로로 밀어 더 많은 열을 볼 수 있어요. 첫 열은 고정됩니다.</p>
</div>
</div>
<p class="mobile-scroll-hint">표를 좌우로 밀어 월별 금액을 확인하세요.</p>
<div class="table-scroll" tabindex="0" role="region" aria-label="월별 수입 지출 잔액">
<table class="data-table">
<caption class="sr-only">월별 수입 지출 잔액</caption>
<thead>
<tr>
<th scope="col">월</th>
<th scope="col">수입</th>
<th scope="col">지출</th>
<th scope="col">그 달 잔액</th>
<th scope="col">누적 잔액</th>
</tr>
</thead>
<tbody id="monthlyBody"></tbody>
</table>
</div>
</section>
<section class="panel">
<div class="panel-head">
<div>
<h2>카테고리별 지출</h2>
<p>그룹 소계는 개별 항목의 합입니다. 전체 합계에 중복해 더하지 않아요.</p>
</div>
</div>
<p class="mobile-scroll-hint">표를 좌우로 밀어 월별 금액을 확인하세요.</p>
<div class="table-scroll" tabindex="0" role="region" aria-label="카테고리별 월별 지출, 단위 원">
<table class="data-table">
<caption class="sr-only">카테고리별 월별 지출, 단위 원</caption>
<thead id="categoryHead"></thead>
<tbody id="categoryBody"></tbody>
</table>
</div>
<p class="table-unit">단위: 원</p>
</section>
<section class="panel">
<div class="panel-head">
<div>
<h2>결제수단별 지출</h2>
<p>어떤 결제수단을 자주 사용했는지 확인해요.</p>
</div>
</div>
<p class="mobile-scroll-hint">표를 좌우로 밀어 월별 금액을 확인하세요.</p>
<div class="table-scroll" tabindex="0" role="region" aria-label="결제수단별 월별 지출, 단위 원">
<table class="data-table">
<caption class="sr-only">결제수단별 월별 지출, 단위 원</caption>
<thead id="paymentHead"></thead>
<tbody id="paymentBody"></tbody>
</table>
</div>
<p class="table-unit">단위: 원</p>
</section>
<div class="chart-grid editor-grid">
<section class="panel">
<div class="panel-head">
<div>
<h2>월별 수입과 지출</h2>
<p id="monthlyChartRange">—</p>
</div>
<div class="chart-legend">
<span><i class="legend-dot"></i>수입</span>
<span><i class="legend-dot expense-dot"></i>지출</span>
</div>
</div>
<div class="chart-wrap">
<canvas id="monthlyChart" aria-label="월별 수입 지출 막대 차트" role="img"></canvas>
</div>
</section>
<section class="panel">
<div class="panel-head">
<div>
<h2>카테고리 비중</h2>
<p>달력 월 기준 · 그룹 단위</p>
</div>
<label class="field">
<span>선택 월</span>
<input type="month" id="categoryMonth">
</label>
</div>
<div class="chart-wrap donut">
<canvas id="categoryChart" aria-label="선택 월 카테고리 비중 도넛 차트" role="img"></canvas>
</div>
<div class="category-legend" id="categoryLegend"></div>
</section>
</div>
</jsp:body>
</t:layout>
