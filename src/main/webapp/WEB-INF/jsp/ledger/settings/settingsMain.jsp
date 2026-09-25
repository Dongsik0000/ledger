<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 설정: 목록·값은 settings.js 가 /ledger/settings/load 로 채운다. 공휴일·내보내기 동작은 holiday.js·settings.js --%>
<t:layout title="설정" page="settings">
<jsp:attribute name="script">
<script defer src="<c:url value='/resources/js/app/settings/settings.js'/>"></script>
<script defer src="<c:url value='/resources/js/app/settings/holiday.js'/>"></script>
</jsp:attribute>
<jsp:body>
<header class="page-head">
<div>
<p class="eyebrow">MAKE IT YOURS</p>
<h1>설정</h1>
<p class="page-description">기록의 기준을 정하고, 익숙한 방식으로 관리해요.</p>
</div>
</header>
<nav class="section-jumps" aria-label="설정 바로가기"><a href="#cycle-settings">주기</a><a href="#export-settings">기록 내보내기</a><a href="#category-settings">카테고리</a><a href="#payment-settings">결제수단</a><a href="#holiday-settings">공휴일</a></nav>
<div class="settings-grid">
<section class="panel">
<div class="panel-head">
<div>
<h2 id="cycle-settings" tabindex="-1">나의 기록 주기</h2>
<p>월급날에 맞춰 한 달의 시작을 정해요.</p>
</div>
<svg class="icon" aria-hidden="true"><use href="#i-calendar"></use></svg>
</div>
<div class="setting-row">
<div>
<strong>주기 시작일</strong>
<p>달력 월 기준으로 사용하려면 1일을 선택해요.</p>
</div>
<label class="field">
<span>매월</span>
<select id="payDay" name="payDay">
<c:forEach var="d" begin="1" end="31"><option value="${d}">${d}일</option></c:forEach>
</select>
</label>
</div>
<div class="setting-row">
<div>
<strong>주말·공휴일이면 직전 평일로</strong>
<p>시작일을 앞당겨 주기를 계산해요.</p>
</div>
<label class="switch">
<input type="checkbox" id="payDayAdjust" name="payDayAdjust" aria-label="주말 공휴일 직전 평일 보정">
<span aria-hidden="true"></span>
</label>
</div>
<p class="section-note">현재 주기 <b id="cycleExample">—</b><br>거래 목록의 달력 월과는 다른 기준이에요.</p>
<div class="form-actions">
<button type="button" class="button primary" id="cycleSave">주기 설정 저장</button>
</div>
</section>
<section class="panel">
<div class="panel-head">
<div>
<h2 id="export-settings" tabindex="-1">기록 내보내기</h2>
<p>필요한 기간의 내역을 CSV로 보관해요.</p>
</div>
<svg class="icon" aria-hidden="true"><use href="#i-download"></use></svg>
</div>
<div class="form-stack">
<div class="field-row">
<label class="field">
<span>시작일</span>
<input type="date" id="exportFrom" name="exportFrom">
</label>
<label class="field">
<span>종료일</span>
<input type="date" id="exportTo" name="exportTo">
</label>
</div>
<p class="form-note">날짜, 구분, 카테고리, 내용, 금액, 결제수단, 메모를 포함해요.</p>
<button type="button" class="button primary" id="exportButton">
<svg class="icon" aria-hidden="true"><use href="#i-download"></use></svg>CSV 내보내기</button>
</div>
</section>
</div>
<section class="panel editor-grid">
<div class="panel-head">
<div>
<h2 id="category-settings" tabindex="-1">카테고리 관리</h2>
<p>이름·그룹·순서를 수정하거나, 숨기거나 삭제할 수 있어요.</p>
</div>
</div>
<h3 class="section-subtitle">지출 카테고리</h3>
<div class="table-scroll" tabindex="0" role="region" aria-label="지출 카테고리 관리">
<table class="mobile-record-table data-table management-table">
<caption class="sr-only">지출 카테고리 이름 그룹 순서 표시 관리</caption>
<thead>
<tr>
<th scope="col">이름</th>
<th scope="col">그룹</th>
<th scope="col">표시 순서</th>
<th scope="col">표시</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody id="expenseCategoryBody"></tbody>
</table>
</div>
<h3 class="section-subtitle">수입 카테고리</h3>
<div class="table-scroll" tabindex="0" role="region" aria-label="수입 카테고리 관리">
<table class="mobile-record-table data-table management-table">
<caption class="sr-only">수입 카테고리 이름 그룹 순서 표시 관리</caption>
<thead>
<tr>
<th scope="col">이름</th>
<th scope="col">그룹</th>
<th scope="col">표시 순서</th>
<th scope="col">표시</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody id="incomeCategoryBody"></tbody>
</table>
</div>
<p class="form-note">순서 숫자를 바꾸거나 위·아래 버튼으로 순서를 조정합니다. 숨긴 항목은 기존 거래에 남아요. 거래에서 쓰는 항목은 삭제 대신 숨길 수 있어요.</p>
<details class="ui-disclosure" id="categoryAdd">
<summary>카테고리 추가<svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg></summary>
<div class="disclosure-body">
<div class="form-stack">
<fieldset class="choice-field">
<legend>구분</legend>
<div class="choice-buttons">
<label><input type="radio" name="newCategoryType" value="EXPENSE" checked><span>지출</span></label>
<label><input type="radio" name="newCategoryType" value="INCOME"><span>수입</span></label>
</div>
</fieldset>
<div class="field-row">
<label class="field">
<span>이름</span>
<input type="text" id="newCategoryName" placeholder="카테고리 이름" maxlength="50">
</label>
<label class="field">
<span>그룹명 (선택)</span>
<input type="text" id="newCategoryGroup" placeholder="같이 묶을 그룹" maxlength="50">
</label>
</div>
<label class="field">
<span>표시 순서 (비우면 맨 뒤)</span>
<input type="number" id="newCategoryOrder" min="0" max="9999" inputmode="numeric">
</label>
<div class="form-actions">
<button type="button" class="button" id="categoryAddCancel">취소</button>
<button type="button" class="button primary" id="categoryAddSave">저장</button>
</div>
</div>
</div>
</details>
</section>
<section class="panel">
<div class="panel-head">
<div>
<h2 id="payment-settings" tabindex="-1">결제수단 관리</h2>
<p>자주 사용하는 방법으로 정리해요.</p>
</div>
</div>
<div class="table-scroll" tabindex="0" role="region" aria-label="결제수단 관리">
<table class="mobile-record-table data-table management-table">
<caption class="sr-only">결제수단 이름 순서 표시 관리</caption>
<thead>
<tr>
<th scope="col">이름</th>
<th scope="col">순서</th>
<th scope="col">표시</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody id="paymentBody"></tbody>
</table>
</div>
<details class="ui-disclosure" id="paymentAdd">
<summary>결제수단 추가<svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg></summary>
<div class="disclosure-body">
<label class="field">
<span>이름</span>
<input type="text" id="newPaymentName" placeholder="예: 생활비 카드" maxlength="50">
</label>
<div class="form-actions">
<button type="button" class="button" id="paymentAddCancel">취소</button>
<button type="button" class="button primary" id="paymentAddSave">저장</button>
</div>
</div>
</details>
</section>
<section class="panel">
<div class="panel-head">
<div>
<h2 id="holiday-settings" tabindex="-1">공휴일 관리</h2>
<p>주기 시작일과 고정 항목의 날짜 보정에 사용해요. 모든 사용자가 함께 써요.</p>
</div>
</div>
<div class="holiday-toolbar">
<label class="field">
<span>연도</span>
<select id="holidayYear" name="holidayYear"></select>
</label>
<button type="button" class="button primary" id="holidayImport">공휴일 가져오기</button>
</div>
<p class="form-note" id="holidayKeyNote" hidden>공휴일 API 키가 설정되지 않아 가져오기를 쓸 수 없어요. 직접 추가는 가능해요.</p>
<div class="table-scroll" tabindex="0" role="region" aria-label="공휴일 날짜 이름 관리">
<table class="mobile-record-table data-table">
<caption class="sr-only">공휴일 날짜 이름 관리</caption>
<thead>
<tr>
<th scope="col">날짜</th>
<th scope="col">공휴일 이름</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody id="holidayBody"></tbody>
</table>
</div>
<details class="ui-disclosure" id="holidayAdd">
<summary>공휴일 직접 추가<svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg></summary>
<div class="disclosure-body">
<div class="field-row">
<label class="field">
<span>날짜</span>
<input type="date" id="holidayDate">
</label>
<label class="field">
<span>이름</span>
<input type="text" id="holidayName" placeholder="공휴일 이름" maxlength="50">
</label>
</div>
<div class="form-actions">
<button type="button" class="button" id="holidayAddCancel">취소</button>
<button type="button" class="button primary" id="holidayAddSave">저장</button>
</div>
</div>
</details>
</section>
</jsp:body>
</t:layout>
