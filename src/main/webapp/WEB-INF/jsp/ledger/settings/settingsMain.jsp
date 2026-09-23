<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 정적 UI 예시. 신규 이벤트·계산·저장 로직 없음. 금액과 날짜는 디자인 확인용. --%>
<t:layout title="설정" page="settings">
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
<svg class="icon" aria-hidden="true">
<use href="#i-calendar">
</use>
</svg>
</div>
<div class="setting-row">
<div>
<strong>주기 시작일</strong>
<p>달력 월 기준으로 사용하려면 1일을 선택해요.</p>
</div>
<label class="field">
<span>매월</span>
<select name="pay_day">
<option>26일</option>
<option>1일</option>
<option>2일</option>
<option>3일</option>
<option>4일</option>
<option>5일</option>
<option>6일</option>
<option>7일</option>
<option>8일</option>
<option>9일</option>
<option>10일</option>
<option>11일</option>
<option>12일</option>
<option>13일</option>
<option>14일</option>
<option>15일</option>
<option>16일</option>
<option>17일</option>
<option>18일</option>
<option>19일</option>
<option>20일</option>
<option>21일</option>
<option>22일</option>
<option>23일</option>
<option>24일</option>
<option>25일</option>
<option>27일</option>
<option>28일</option>
<option>29일</option>
<option>30일</option>
<option>31일</option>
</select>
</label>
</div>
<div class="setting-row">
<div>
<strong>주말·공휴일이면 직전 평일로</strong>
<p>시작일을 앞당겨 주기를 계산해요.</p>
</div>
<label class="switch">
<input type="checkbox" name="pay_day_adjust" aria-label="주말 공휴일 직전 평일 보정">
<span aria-hidden="true">
</span>
</label>
</div>
<p class="section-note">현재 주기 예시 <b>9월 26일~10월 25일</b>
<br>거래 목록의 달력 월과는 다른 기준이에요.</p>
<div class="form-actions">
<button type="button" class="button primary" >주기 설정 저장</button>
</div>
</section>
<section class="panel">
<div class="panel-head">
<div>
<h2 id="export-settings" tabindex="-1">기록 내보내기</h2>
<p>필요한 기간의 내역을 CSV로 보관해요.</p>
</div>
<svg class="icon" aria-hidden="true">
<use href="#i-download">
</use>
</svg>
</div>
<div class="form-stack">
<div class="field-row">
<label class="field">
<span>시작일</span>
<input type="date" name="exportFrom" value="2026-10-01" >
</label>
<label class="field">
<span>종료일</span>
<input type="date" name="exportTo" value="2026-10-31" >
</label>
</div>
<p class="form-note">날짜, 구분, 카테고리, 내용, 금액, 결제수단, 메모를 포함해요.</p>
<button type="button" class="button primary" >
<svg class="icon" aria-hidden="true">
<use href="#i-download">
</use>
</svg>CSV 내보내기</button>
</div>
</section>
</div>
<section class="panel editor-grid">
<div class="panel-head">
<div>
<h2 id="category-settings" tabindex="-1">카테고리 관리</h2>
<p>이름·그룹·순서를 수정하거나 표시를 꺼서 숨길 수 있어요.</p>
</div>
</div>
<h3 class="section-subtitle">지출 카테고리</h3>
<div class="table-scroll" tabindex="0" role="region" aria-label="카테고리 이름 그룹 순서 표시 관리">
<table class="mobile-record-table data-table management-table">
<caption class="sr-only">카테고리 이름 그룹 순서 표시 관리</caption>
<thead>
<tr>
<th scope="col">이름</th>
<th scope="col">그룹</th>
<th scope="col">표시 순서</th>
<th scope="col">표시</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody>
<tr>
<th scope="row">
<label class="field">
<span>이름</span>
<input type="text" name="expense-0name" value="식비" >
</label>
</th>
<td data-label="그룹">
<label class="field">
<span>그룹명</span>
<input type="text" name="expense-0group" value="식비" placeholder="없음">
</label>
</td>
<td data-label="표시 순서">
<div class="order-control">
<label class="field">
<span>순서</span>
<input type="number" name="expense-0order" value="1" min="1" inputmode="numeric">
</label>
<button type="button" class="button small" aria-label="식비 위로 이동">↑</button>
<button type="button" class="button small" aria-label="식비 아래로 이동">↓</button>
</div>
</td>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="expense-0visible" aria-label="식비 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >저장</button>
</td>
</tr>
<tr>
<th scope="row">
<label class="field">
<span>이름</span>
<input type="text" name="expense-1name" value="배달" >
</label>
</th>
<td data-label="그룹">
<label class="field">
<span>그룹명</span>
<input type="text" name="expense-1group" value="식비" placeholder="없음">
</label>
</td>
<td data-label="표시 순서">
<div class="order-control">
<label class="field">
<span>순서</span>
<input type="number" name="expense-1order" value="2" min="1" inputmode="numeric">
</label>
<button type="button" class="button small" aria-label="배달 위로 이동">↑</button>
<button type="button" class="button small" aria-label="배달 아래로 이동">↓</button>
</div>
</td>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="expense-1visible" aria-label="배달 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >저장</button>
</td>
</tr>
<tr>
<th scope="row">
<label class="field">
<span>이름</span>
<input type="text" name="expense-2name" value="카페/간식" >
</label>
</th>
<td data-label="그룹">
<label class="field">
<span>그룹명</span>
<input type="text" name="expense-2group" value="식비" placeholder="없음">
</label>
</td>
<td data-label="표시 순서">
<div class="order-control">
<label class="field">
<span>순서</span>
<input type="number" name="expense-2order" value="3" min="1" inputmode="numeric">
</label>
<button type="button" class="button small" aria-label="카페/간식 위로 이동">↑</button>
<button type="button" class="button small" aria-label="카페/간식 아래로 이동">↓</button>
</div>
</td>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="expense-2visible" aria-label="카페/간식 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >저장</button>
</td>
</tr>
<tr>
<th scope="row">
<label class="field">
<span>이름</span>
<input type="text" name="expense-3name" value="교통" >
</label>
</th>
<td data-label="그룹">
<label class="field">
<span>그룹명</span>
<input type="text" name="expense-3group" placeholder="없음">
</label>
</td>
<td data-label="표시 순서">
<div class="order-control">
<label class="field">
<span>순서</span>
<input type="number" name="expense-3order" value="4" min="1" inputmode="numeric">
</label>
<button type="button" class="button small" aria-label="교통 위로 이동">↑</button>
<button type="button" class="button small" aria-label="교통 아래로 이동">↓</button>
</div>
</td>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="expense-3visible" aria-label="교통 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >저장</button>
</td>
</tr>
<tr>
<th scope="row">
<label class="field">
<span>이름</span>
<input type="text" name="expense-4name" value="저축" >
</label>
</th>
<td data-label="그룹">
<label class="field">
<span>그룹명</span>
<input type="text" name="expense-4group" placeholder="없음">
</label>
</td>
<td data-label="표시 순서">
<div class="order-control">
<label class="field">
<span>순서</span>
<input type="number" name="expense-4order" value="5" min="1" inputmode="numeric">
</label>
<button type="button" class="button small" aria-label="저축 위로 이동">↑</button>
<button type="button" class="button small" aria-label="저축 아래로 이동">↓</button>
</div>
</td>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="expense-4visible" aria-label="저축 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >저장</button>
</td>
</tr>
<tr>
<th scope="row">
<label class="field">
<span>이름</span>
<input type="text" name="expense-5name" value="기타" >
</label>
</th>
<td data-label="그룹">
<label class="field">
<span>그룹명</span>
<input type="text" name="expense-5group" placeholder="없음">
</label>
</td>
<td data-label="표시 순서">
<div class="order-control">
<label class="field">
<span>순서</span>
<input type="number" name="expense-5order" value="6" min="1" inputmode="numeric">
</label>
<button type="button" class="button small" aria-label="기타 위로 이동">↑</button>
<button type="button" class="button small" aria-label="기타 아래로 이동">↓</button>
</div>
</td>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="expense-5visible" aria-label="기타 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >저장</button>
</td>
</tr>
</tbody>
</table>
</div>
<h3 class="section-subtitle">수입 카테고리</h3>
<div class="table-scroll" tabindex="0" role="region" aria-label="카테고리 이름 그룹 순서 표시 관리">
<table class="mobile-record-table data-table management-table">
<caption class="sr-only">카테고리 이름 그룹 순서 표시 관리</caption>
<thead>
<tr>
<th scope="col">이름</th>
<th scope="col">그룹</th>
<th scope="col">표시 순서</th>
<th scope="col">표시</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody>
<tr>
<th scope="row">
<label class="field">
<span>이름</span>
<input type="text" name="income-0name" value="급여" >
</label>
</th>
<td data-label="그룹">
<label class="field">
<span>그룹명</span>
<input type="text" name="income-0group" placeholder="없음">
</label>
</td>
<td data-label="표시 순서">
<div class="order-control">
<label class="field">
<span>순서</span>
<input type="number" name="income-0order" value="1" min="1" inputmode="numeric">
</label>
<button type="button" class="button small" aria-label="급여 위로 이동">↑</button>
<button type="button" class="button small" aria-label="급여 아래로 이동">↓</button>
</div>
</td>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="income-0visible" aria-label="급여 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >저장</button>
</td>
</tr>
<tr>
<th scope="row">
<label class="field">
<span>이름</span>
<input type="text" name="income-1name" value="기타" >
</label>
</th>
<td data-label="그룹">
<label class="field">
<span>그룹명</span>
<input type="text" name="income-1group" placeholder="없음">
</label>
</td>
<td data-label="표시 순서">
<div class="order-control">
<label class="field">
<span>순서</span>
<input type="number" name="income-1order" value="2" min="1" inputmode="numeric">
</label>
<button type="button" class="button small" aria-label="기타 위로 이동">↑</button>
<button type="button" class="button small" aria-label="기타 아래로 이동">↓</button>
</div>
</td>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="income-1visible" aria-label="기타 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >저장</button>
</td>
</tr>
</tbody>
</table>
</div>
<p class="form-note">순서 숫자를 바꾸거나 위·아래 버튼으로 순서를 조정합니다. 숨긴 항목은 기존 거래에는 남아요.</p>
<details class="ui-disclosure ">
<summary>카테고리 추가<svg class="icon" aria-hidden="true">
<use href="#i-plus">
</use>
</svg>
</summary>
<div class="disclosure-body">
<div class="form-stack">
<fieldset class="choice-field">
<legend>구분</legend>
<div class="choice-buttons">
<label>
<input type="radio" name="new-category-type" value="EXPENSE" checked>
<span>지출</span>
</label>
<label>
<input type="radio" name="new-category-type" value="INCOME">
<span>수입</span>
</label>
</div>
</fieldset>
<div class="field-row">
<label class="field">
<span>이름</span>
<input type="text" name="new-category-name" placeholder="카테고리 이름">
</label>
<label class="field">
<span>그룹명 (선택)</span>
<input type="text" name="new-category-group" placeholder="같이 묶을 그룹">
</label>
</div>
<label class="field">
<span>표시 순서</span>
<input type="number" name="new-category-order" value="7" min="1">
</label>
<div class="form-actions">
<button type="button" class="button " >취소</button>
<button type="button" class="button primary" >저장</button>
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
<div class="table-scroll" tabindex="0" role="region" aria-label="결제수단 수정 숨김">
<table class="mobile-record-table data-table management-table">
<caption class="sr-only">결제수단 수정 숨김</caption>
<thead>
<tr>
<th scope="col">이름</th>
<th scope="col">표시</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody>
<tr>
<th scope="row">
<label class="field">
<span>결제수단 이름</span>
<input type="text" name="payment-0" value="신용카드" >
</label>
</th>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="payment-visible-0" aria-label="신용카드 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >수정 저장</button>
</td>
</tr>
<tr>
<th scope="row">
<label class="field">
<span>결제수단 이름</span>
<input type="text" name="payment-1" value="체크카드" >
</label>
</th>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="payment-visible-1" aria-label="체크카드 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >수정 저장</button>
</td>
</tr>
<tr>
<th scope="row">
<label class="field">
<span>결제수단 이름</span>
<input type="text" name="payment-2" value="계좌이체" >
</label>
</th>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="payment-visible-2" aria-label="계좌이체 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >수정 저장</button>
</td>
</tr>
<tr>
<th scope="row">
<label class="field">
<span>결제수단 이름</span>
<input type="text" name="payment-3" value="현금" >
</label>
</th>
<td data-label="표시">
<label class="switch">
<input type="checkbox" name="payment-visible-3" aria-label="현금 표시" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small" >수정 저장</button>
</td>
</tr>
</tbody>
</table>
</div>
<details class="ui-disclosure ">
<summary>결제수단 추가<svg class="icon" aria-hidden="true">
<use href="#i-plus">
</use>
</svg>
</summary>
<div class="disclosure-body">
<label class="field">
<span>이름</span>
<input type="text" name="new-payment-name" placeholder="예: 생활비 카드">
</label>
<div class="form-actions">
<button type="button" class="button " >취소</button>
<button type="button" class="button primary" >저장</button>
</div>
</div>
</details>
</section>
<section class="panel">
<div class="panel-head">
<div>
<h2 id="holiday-settings" tabindex="-1">공휴일 관리</h2>
<p>주기 시작일과 고정 항목의 날짜 보정에 사용해요.</p>
</div>
</div>
<div class="holiday-toolbar">
<label class="field">
<span>연도</span>
<select name="holidayYear">
<option>2026년</option>
<option>2027년</option>
<option>2028년</option>
</select>
</label>
<button type="button" class="button primary" >공휴일 가져오기</button>
</div>
<div class="table-scroll" tabindex="0" role="region" aria-label="공휴일 날짜 이름 관리">
<table class="mobile-record-table data-table ">
<caption class="sr-only">공휴일 날짜 이름 관리</caption>
<thead>
<tr>
<th scope="col">날짜</th>
<th scope="col">공휴일 이름</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody>
<tr>
<th scope="row">2026.10.03</th>
<td data-label="공휴일 이름">개천절</td>
<td data-label="관리">
<button type="button" class="button small danger" >삭제</button>
</td>
</tr>
<tr>
<th scope="row">2026.10.05</th>
<td data-label="공휴일 이름">대체공휴일</td>
<td data-label="관리">
<button type="button" class="button small danger" >삭제</button>
</td>
</tr>
<tr>
<th scope="row">2026.10.09</th>
<td data-label="공휴일 이름">한글날</td>
<td data-label="관리">
<button type="button" class="button small danger" >삭제</button>
</td>
</tr>
</tbody>
</table>
</div>
<details class="ui-disclosure ">
<summary>공휴일 직접 추가<svg class="icon" aria-hidden="true">
<use href="#i-plus">
</use>
</svg>
</summary>
<div class="disclosure-body">
<div class="field-row">
<label class="field">
<span>날짜</span>
<input type="date" name="holidayDate" >
</label>
<label class="field">
<span>이름</span>
<input type="text" name="holidayName" placeholder="공휴일 이름">
</label>
</div>
<div class="form-actions">
<button type="button" class="button " >취소</button>
<button type="button" class="button primary" >저장</button>
</div>
</div>
</details>
</section>
</jsp:body>
</t:layout>
