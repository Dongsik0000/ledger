<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 거래 내역: 목록·필터 선택지는 entry.js 가 채운다 --%>
<t:layout title="거래 내역" page="entry">
<jsp:attribute name="script">
<script defer src="<c:url value='/resources/js/app/entry/entryForm.js'/>"></script>
<script defer src="<c:url value='/resources/js/app/entry/entry.js'/>"></script>
</jsp:attribute>
<jsp:body>
<header class="page-head">
<div>
<p class="eyebrow">MY TRANSACTIONS</p>
<h1>거래 내역</h1>
<p class="page-description">언제, 어디에 썼는지 차분히 돌아봐요.</p>
</div>
<button type="button" class="button primary" id="entryNew">
<svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>거래 추가</button>
</header>
<section class="panel reveal">
<div class="panel-head">
<div>
<h2 id="monthTitle">—</h2>
<p id="monthRange">달력 월 기준</p>
</div>
<div class="period-control">
<label class="field">
<span>기간</span>
<select id="period">
<option value="MONTH">달력 월</option>
<option value="RECENT12">최근 12개월</option>
<option value="ALL">전체</option>
</select>
</label>
<button type="button" class="button" id="prevMonth" aria-label="이전 달">
<svg class="icon" aria-hidden="true"><use href="#i-left"></use></svg>
</button>
<label class="field" id="monthField">
<span>조회 월</span>
<input type="month" id="month" name="month">
</label>
<button type="button" class="button" id="nextMonth" aria-label="다음 달">
<svg class="icon" aria-hidden="true"><use href="#i-right"></use></svg>
</button>
</div>
</div>
<details class="filter-disclosure"><summary>검색 · 필터 <span>수입·지출 / 분류</span></summary><div class="filter-body"><div class="filters">
<label class="field">
<span>검색</span>
<input type="search" id="filterKeyword" placeholder="내용이나 메모 검색" maxlength="50">
</label>
<label class="field">
<span>구분</span>
<select id="filterType">
<option value="">전체</option>
<option value="EXPENSE">지출</option>
<option value="INCOME">수입</option>
</select>
</label>
<label class="field">
<span>카테고리</span>
<select id="filterCategory"><option value="">전체</option></select>
</label>
<label class="field">
<span>결제수단</span>
<select id="filterPayment"><option value="">전체</option></select>
</label>
</div>
<div class="filter-actions">
<button type="button" class="button" id="filterReset">초기화</button>
<button type="button" class="button primary" id="filterSearch">
<svg class="icon" aria-hidden="true"><use href="#i-search"></use></svg>조회</button>
</div>
</div></details>
<div class="list-heading">
<span id="listCount">—</span>
<span id="listTotals" aria-live="polite"></span>
</div>
<p class="form-note" id="listTruncated" hidden>최근 500건까지만 보여요. 검색어나 필터로 좁혀 보세요. 위 합계는 조건에 맞는 전체 거래 기준이에요.</p>
<div id="entryList"></div>
</section>

<t:entryEditor id="entryEditor"/>
</jsp:body>
</t:layout>
