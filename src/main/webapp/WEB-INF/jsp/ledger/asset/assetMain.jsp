<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 정적 UI 예시. 신규 이벤트·계산·저장 로직 없음. 금액과 날짜는 디자인 확인용. --%>
<t:layout title="자산" page="asset">
<jsp:body>
<header class="page-head">
<div>
<p class="eyebrow">MY LITTLE GROWTH</p>
<h1>자산</h1>
<p class="page-description">지금의 잔액을 기록하고, 나의 여유를 살펴봐요.</p>
</div>
<button type="button" class="button primary" commandfor="asset-new" command="show-modal">
<svg class="icon" aria-hidden="true">
<use href="#i-plus">
</use>
</svg>자산 추가</button>
</header>
<div class="three-metrics">
<article class="panel metric featured">
<div class="metric-label">나의 총 잔액<svg class="icon" aria-hidden="true">
<use href="#i-wallet">
</use>
</svg>
</div>
<p class="metric-value">8,980,000<small>원</small>
</p>
<p class="metric-foot">가계부 잔액 + 자산 합계</p>
</article>
<article class="panel metric sage">
<div class="metric-label">자산 합계<svg class="icon" aria-hidden="true">
<use href="#i-leaf">
</use>
</svg>
</div>
<p class="metric-value">7,050,000<small>원</small>
</p>
<p class="metric-foot">직접 기록한 잔액의 합</p>
</article>
<article class="panel metric ">
<div class="metric-label">가계부 잔액<svg class="icon" aria-hidden="true">
<use href="#i-book">
</use>
</svg>
</div>
<p class="metric-value">1,930,000<small>원</small>
</p>
<p class="metric-foot">현재 주기 잔액 · 이월 포함</p>
</article>
</div>
<section class="panel">
<div class="panel-head">
<div>
<h2>나의 자산</h2>
<p>잔액은 직접 입력하고 갱신해요.</p>
</div>
</div>
<div class="table-scroll" tabindex="0" role="region" aria-label="자산 이름, 잔액, 갱신일">
<table class="mobile-record-table data-table ">
<caption class="sr-only">자산 이름, 잔액, 갱신일</caption>
<thead>
<tr>
<th scope="col">자산 이름</th>
<th scope="col">현재 잔액</th>
<th scope="col">마지막 갱신일</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody>
<tr>
<th scope="row">비상금 통장</th>
<td data-label="현재 잔액">3,000,000원</td>
<td data-label="마지막 갱신일">2026.10.08</td>
<td data-label="관리">
<button type="button" class="button small edit-button" commandfor="asset-edit-1" command="show-modal" aria-label="비상금 통장 수정">수정</button>
<button type="button" class="button small danger" commandfor="asset-edit-1-delete" command="show-modal">삭제…</button>
</td>
</tr>
<tr>
<th scope="row">차곡차곡 적금</th>
<td data-label="현재 잔액">2,400,000원</td>
<td data-label="마지막 갱신일">2026.10.06</td>
<td data-label="관리">
<button type="button" class="button small edit-button" commandfor="asset-edit-2" command="show-modal" aria-label="차곡차곡 적금 수정">수정</button>
<button type="button" class="button small danger" commandfor="asset-edit-2-delete" command="show-modal">삭제…</button>
</td>
</tr>
<tr>
<th scope="row">투자 계좌</th>
<td data-label="현재 잔액">1,650,000원</td>
<td data-label="마지막 갱신일">2026.10.01</td>
<td data-label="관리">
<button type="button" class="button small edit-button" commandfor="asset-edit-3" command="show-modal" aria-label="투자 계좌 수정">수정</button>
<button type="button" class="button small danger" commandfor="asset-edit-3-delete" command="show-modal">삭제…</button>
</td>
</tr>
</tbody>
</table>
</div>
</section>
<aside class="section-note">
<strong>
<svg class="icon" aria-hidden="true">
<use href="#i-info">
</use>
</svg>적금으로 옮긴 돈은 지출로도 기록해 주세요.</strong>가계부에서 적금·주식 등으로 옮긴 금액은 ‘저축’ 카테고리의 지출로 기록하고, 자산 잔액에도 반영해 주세요. 같은 돈이 가계부와 자산에 중복 합산되지 않게 하기 위한 규칙이에요.</aside>

<t:assetEditor id="asset-new" heading="자산 추가" subtitle="필요한 내용을 입력해 주세요." name="" amount="" editing="false"/>
<t:assetEditor id="asset-edit-1" heading="자산 수정" subtitle="비상금 통장" editing="true" name="비상금 통장" amount="3000000"/>
<t:assetEditor id="asset-edit-2" heading="자산 수정" subtitle="차곡차곡 적금" editing="true" name="차곡차곡 적금" amount="2400000"/>
<t:assetEditor id="asset-edit-3" heading="자산 수정" subtitle="투자 계좌" editing="true" name="투자 계좌" amount="1650000"/>
</jsp:body>
</t:layout>
