<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 고정 항목: 목록·지표는 recurring.js 가 /ledger/recurring/list 와 /ledger/dashboard/summary 로 채운다 --%>
<t:layout title="고정 항목" page="recurring">
<jsp:attribute name="script">
<script defer src="<c:url value='/resources/js/app/recurring/recurring.js'/>"></script>
</jsp:attribute>
<jsp:body>
<header class="page-head">
<div>
<p class="eyebrow">ON REPEAT</p>
<h1>고정 항목</h1>
<p class="page-description">구독부터 월급까지, 반복되는 돈의 흐름을 챙겨요.</p>
</div>
<button type="button" class="button primary" id="recurringNew">
<svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>고정 항목 추가</button>
</header>
<div class="cycle-strip">
<span>
<svg class="icon" aria-hidden="true"><use href="#i-calendar"></use></svg>현재 주기 <strong id="cycleRange">—</strong>
</span>
<span class="chip" id="daysLeft">—</span>
</div>
<div class="three-metrics">
<article class="panel metric featured">
<div class="metric-label">주기 고정지출<svg class="icon" aria-hidden="true"><use href="#i-wallet"></use></svg></div>
<p class="metric-value"><span id="monthlyFixed">—</span><small>원</small></p>
<p class="metric-foot">활성화된 고정지출</p>
</article>
<article class="panel metric sage">
<div class="metric-label">아직 예정인 지출<svg class="icon" aria-hidden="true"><use href="#i-calendar"></use></svg></div>
<p class="metric-value"><span id="pendingFixed">—</span><small>원</small></p>
<p class="metric-foot">오늘 쓸 수 있는 돈에서 제외</p>
</article>
<article class="panel metric">
<div class="metric-label">이미 기록된 지출<svg class="icon" aria-hidden="true"><use href="#i-repeat"></use></svg></div>
<p class="metric-value"><span id="recorded">—</span><small>원</small></p>
<p class="metric-foot">이번 주기에 처리된 금액</p>
</article>
</div>
<section class="panel">
<div class="panel-head">
<div>
<h2>고정 항목 목록</h2>
<p>주기 기준 처리 상태와 활성 여부를 구분해요.</p>
</div>
</div>
<div class="table-scroll" tabindex="0" role="region" aria-label="고정 항목 목록">
<table class="mobile-record-table data-table recurring-table">
<caption class="sr-only">고정 항목 목록</caption>
<thead>
<tr>
<th scope="col">항목 / 구분</th>
<th scope="col">금액</th>
<th scope="col">분류 / 결제수단</th>
<th scope="col">매월 결제일</th>
<th scope="col">휴일 보정</th>
<th scope="col">다음 결제일</th>
<th scope="col">이번 주기</th>
<th scope="col">활성</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody id="recurringBody"></tbody>
</table>
</div>
</section>

<t:recurringEditor id="recurringEditor"/>
</jsp:body>
</t:layout>
