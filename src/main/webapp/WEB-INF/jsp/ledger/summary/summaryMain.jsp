<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 정적 UI 예시. 신규 이벤트·계산·저장 로직 없음. 금액과 날짜는 디자인 확인용. --%>
<t:layout title="요약" page="summary">
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
<span>시작 월</span>
<input type="month" name="from" value="2026-08" >
</label>
<span aria-hidden="true">—</span>
<label class="field">
<span>종료 월</span>
<input type="month" name="to" value="2026-10" >
</label>
<button type="button" class="button primary" >조회</button>
</div>
<p class="form-note">아래 월별 표·차트는 달력 월 기준입니다. 현재 주기 잔액은 별도로 표시합니다.</p>
</section>
<div class="cycle-strip">
<span>
<svg class="icon" aria-hidden="true">
<use href="#i-calendar">
</use>
</svg>현재 주기 <strong>2026.09.26 — 10.25</strong>
</span>
<span class="chip">18일 남음</span>
</div>
<div class="three-metrics">
<article class="panel metric featured">
<div class="metric-label">현재 주기 잔액<svg class="icon" aria-hidden="true">
<use href="#i-wallet">
</use>
</svg>
</div>
<p class="metric-value">1,930,000<small>원</small>
</p>
<p class="metric-foot">9/26~10/25 · 이월 포함</p>
</article>
<article class="panel metric ">
<div class="metric-label">현재 주기 수입<svg class="icon" aria-hidden="true">
<use href="#i-chart">
</use>
</svg>
</div>
<p class="metric-value">3,200,000<small>원</small>
</p>
<p class="metric-foot">9/26부터 기준일까지</p>
</article>
<article class="panel metric sage">
<div class="metric-label">현재 주기 지출<svg class="icon" aria-hidden="true">
<use href="#i-book">
</use>
</svg>
</div>
<p class="metric-value">1,750,000<small>원</small>
</p>
<p class="metric-foot">9/26부터 기준일까지</p>
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
<table class="data-table ">
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
<tbody>
<tr>
<th scope="row">2026.08</th>
<td>3,200,000원</td>
<td>2,950,000원</td>
<td>+250,000원</td>
<td>480,000원</td>
</tr>
<tr>
<th scope="row">2026.09</th>
<td>3,200,000원</td>
<td>3,490,000원</td>
<td>
<span class="expense">−290,000원</span>
</td>
<td>190,000원</td>
</tr>
<tr>
<th scope="row">2026.10</th>
<td>3,200,000원</td>
<td>1,460,000원</td>
<td>+1,740,000원</td>
<td>1,930,000원</td>
</tr>
</tbody>
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
<table class="data-table ">
<caption class="sr-only">카테고리별 월별 지출, 단위 원</caption>
<thead>
<tr>
<th scope="col">카테고리</th>
<th scope="col">8월</th>
<th scope="col">9월</th>
<th scope="col">10월</th>
<th scope="col">기간 합계</th>
</tr>
</thead>
<tbody>
<tr>
<th scope="row">식비</th>
<td>200,000</td>
<td>230,000</td>
<td>180,000</td>
<td>610,000</td>
</tr>
<tr>
<th scope="row">배달</th>
<td>100,000</td>
<td>120,000</td>
<td>110,000</td>
<td>330,000</td>
</tr>
<tr>
<th scope="row">카페/간식</th>
<td>60,000</td>
<td>70,000</td>
<td>70,000</td>
<td>200,000</td>
</tr>
<tr class="subtotal">
<th scope="row">식비 그룹 소계</th>
<td>360,000</td>
<td>420,000</td>
<td>360,000</td>
<td>1,140,000</td>
</tr>
<tr>
<th scope="row">고정지출</th>
<td>700,000</td>
<td>700,000</td>
<td>700,000</td>
<td>2,100,000</td>
</tr>
<tr>
<th scope="row">저축</th>
<td>200,000</td>
<td>200,000</td>
<td>200,000</td>
<td>600,000</td>
</tr>
<tr>
<th scope="row">기타</th>
<td>1,690,000</td>
<td>2,170,000</td>
<td>200,000</td>
<td>4,060,000</td>
</tr>
<tr class="subtotal">
<th scope="row">합계</th>
<td>2,950,000</td>
<td>3,490,000</td>
<td>1,460,000</td>
<td>7,900,000</td>
</tr>
</tbody>
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
<table class="data-table ">
<caption class="sr-only">결제수단별 월별 지출, 단위 원</caption>
<thead>
<tr>
<th scope="col">결제수단</th>
<th scope="col">8월</th>
<th scope="col">9월</th>
<th scope="col">10월</th>
<th scope="col">기간 합계</th>
</tr>
</thead>
<tbody>
<tr>
<th scope="row">신용카드</th>
<td>1,500,000</td>
<td>2,000,000</td>
<td>550,000</td>
<td>4,050,000</td>
</tr>
<tr>
<th scope="row">체크카드</th>
<td>500,000</td>
<td>540,000</td>
<td>260,000</td>
<td>1,300,000</td>
</tr>
<tr>
<th scope="row">계좌이체</th>
<td>900,000</td>
<td>900,000</td>
<td>600,000</td>
<td>2,400,000</td>
</tr>
<tr>
<th scope="row">현금</th>
<td>50,000</td>
<td>50,000</td>
<td>50,000</td>
<td>150,000</td>
</tr>
<tr class="subtotal">
<th scope="row">합계</th>
<td>2,950,000</td>
<td>3,490,000</td>
<td>1,460,000</td>
<td>7,900,000</td>
</tr>
</tbody>
</table>
</div>
<p class="table-unit">단위: 원</p>
</section>
<div class="chart-grid editor-grid">
<section class="panel">
<div class="panel-head">
<div>
<h2>월별 수입과 지출</h2>
<p>2026년 8월~10월</p>
</div>
<div class="chart-legend">
<span>
<i class="legend-dot">
</i>수입</span>
<span>
<i class="legend-dot expense-dot">
</i>지출</span>
</div>
</div>
<div class="static-chart">
<canvas id="monthlyChart" hidden aria-label="월별 수입 지출 차트" role="img">
</canvas>
<svg viewBox="0 0 520 205" role="img" aria-label="정적 예시: 8월부터 10월까지 수입과 지출 막대그래프">
<g stroke="#e8ece1" stroke-width="1">
<path d="M45 25H510"/>
<path d="M45 70H510"/>
<path d="M45 115H510"/>
<path d="M45 160H510"/>
</g>
<g fill="#6c7664" font-size="10">
<text x="0" y="29">400만</text>
<text x="0" y="74">300만</text>
<text x="0" y="119">200만</text>
<text x="0" y="164">100만</text>
</g>
<rect class="bar-shape" x="105" y="59" width="27" height="116" rx="5" fill="#547851"/>
<rect class="bar-shape" x="139" y="68" width="27" height="107" rx="5" fill="#bc9856"/>
<text x="134" y="198" text-anchor="middle" fill="#65705d" font-size="12">8월</text>
<rect class="bar-shape" x="255" y="59" width="27" height="116" rx="5" fill="#547851"/>
<rect class="bar-shape" x="289" y="48" width="27" height="127" rx="5" fill="#bc9856"/>
<text x="284" y="198" text-anchor="middle" fill="#65705d" font-size="12">9월</text>
<rect class="bar-shape" x="405" y="59" width="27" height="116" rx="5" fill="#547851"/>
<rect class="bar-shape" x="439" y="122" width="27" height="53" rx="5" fill="#bc9856"/>
<text x="434" y="198" text-anchor="middle" fill="#65705d" font-size="12">10월</text>
</svg>
</div>
</section>
<section class="panel">
<div class="panel-head">
<div>
<h2>카테고리 비중</h2>
<p>달력 월 기준</p>
</div>
<label class="field">
<span>선택 월</span>
<input type="month" name="categoryMonth" value="2026-10" >
</label>
</div>
<div class="static-chart">
<canvas id="categoryChart" hidden aria-label="선택 월 카테고리 비중 차트" role="img">
</canvas>
<svg class="donut-example" viewBox="0 0 220 220" role="img" aria-label="정적 예시: 고정지출 70만원, 식비 그룹 36만원, 저축 20만원, 기타 20만원">
<circle cx="110" cy="110" r="78" fill="none" stroke="#ecefe4" stroke-width="26"/>
<g transform="rotate(-90 110 110)" fill="none" stroke-width="26">
<circle cx="110" cy="110" r="78" pathLength="100" stroke="#547851" stroke-dasharray="46.95 53.05"/>
<circle cx="110" cy="110" r="78" pathLength="100" stroke="#bc9856" stroke-dasharray="23.66 76.34" stroke-dashoffset="-47.95"/>
<circle cx="110" cy="110" r="78" pathLength="100" stroke="#879975" stroke-dasharray="12.7 87.3" stroke-dashoffset="-72.61"/>
<circle cx="110" cy="110" r="78" pathLength="100" stroke="#a27560" stroke-dasharray="12.7 87.3" stroke-dashoffset="-86.31"/>
</g>
<text x="110" y="100" text-anchor="middle" font-size="12" fill="#65705d">10월 지출</text>
<text x="110" y="123" text-anchor="middle" font-size="19" fill="#263a30" font-weight="600">1,460,000원</text>
</svg>
</div>
<div class="category-legend">
<span>고정지출 <b>700,000원</b>
</span>
<span>식비 그룹 <b>360,000원</b>
</span>
<span>저축 <b>200,000원</b>
</span>
<span>기타 <b>200,000원</b>
</span>
</div>
</section>
</div>
</jsp:body>
</t:layout>
