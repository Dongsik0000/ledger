<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 자산: 목록은 asset.js 가 /ledger/asset/list, 가계부 잔액은 /ledger/dashboard/summary 로 채운다 --%>
<t:layout title="자산" page="asset">
<jsp:attribute name="script">
<script defer src="<c:url value='/resources/js/app/asset/asset.js'/>"></script>
</jsp:attribute>
<jsp:body>
<header class="page-head">
<div>
<p class="eyebrow">MY LITTLE GROWTH</p>
<h1>자산</h1>
<p class="page-description">지금의 잔액을 기록하고, 나의 여유를 살펴봐요.</p>
</div>
<button type="button" class="button primary" id="assetNew">
<svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>자산 추가</button>
</header>
<div class="three-metrics">
<article class="panel metric featured">
<div class="metric-label">나의 총 잔액<svg class="icon" aria-hidden="true"><use href="#i-wallet"></use></svg></div>
<p class="metric-value"><span id="totalBalance">—</span><small>원</small></p>
<p class="metric-foot">가계부 잔액 + 자산 합계</p>
</article>
<article class="panel metric sage">
<div class="metric-label">자산 합계<svg class="icon" aria-hidden="true"><use href="#i-leaf"></use></svg></div>
<p class="metric-value"><span id="assetTotal">—</span><small>원</small></p>
<p class="metric-foot">직접 기록한 잔액 + 미국 주식 평가액</p>
</article>
<article class="panel metric">
<div class="metric-label">가계부 잔액<svg class="icon" aria-hidden="true"><use href="#i-book"></use></svg></div>
<p class="metric-value"><span id="ledgerBalance">—</span><small>원</small></p>
<p class="metric-foot">현재 주기 잔액 · 이월 포함</p>
</article>
</div>
<section class="panel">
<div class="panel-head">
<div>
<h2>나의 자산</h2>
<p>원화 잔액을 기록하거나 미국 주식 종목·수량으로 평가해요.</p>
</div>
</div>
<div class="table-scroll" tabindex="0" role="region" aria-label="자산 이름, 잔액, 갱신일">
<table class="mobile-record-table data-table">
<caption class="sr-only">자산 이름, 잔액, 갱신일</caption>
<thead>
<tr>
<th scope="col">자산 이름</th>
<th scope="col">현재 잔액</th>
<th scope="col">마지막 갱신일</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody id="assetBody"></tbody>
</table>
</div>
<p class="form-note" id="assetMarketStatus" role="status"></p>
</section>
<aside class="section-note">
<strong>
<svg class="icon" aria-hidden="true"><use href="#i-info"></use></svg>적금 납입은 자산으로 이체해 주세요.</strong>고정 항목이나 지출 거래에서 적금 자산을 선택하면, 지출 기록과 자산 잔액 증가가 함께 처리돼요. 연결한 납입액을 자산 잔액에 다시 수동으로 더하지 마세요.</aside>

<t:assetEditor id="assetEditor"/>
</jsp:body>
</t:layout>
