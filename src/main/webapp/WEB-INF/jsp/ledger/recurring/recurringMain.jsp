<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 정적 UI 예시. 신규 이벤트·계산·저장 로직 없음. 금액과 날짜는 디자인 확인용. --%>
<t:layout title="고정 항목" page="recurring">
<jsp:body>
<header class="page-head">
<div>
<p class="eyebrow">ON REPEAT</p>
<h1>고정 항목</h1>
<p class="page-description">구독부터 월급까지, 반복되는 돈의 흐름을 챙겨요.</p>
</div>
<button type="button" class="button primary" commandfor="recurring-new" command="show-modal">
<svg class="icon" aria-hidden="true">
<use href="#i-plus">
</use>
</svg>고정 항목 추가</button>
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
<div class="three-metrics">
<article class="panel metric featured">
<div class="metric-label">주기 고정지출<svg class="icon" aria-hidden="true">
<use href="#i-wallet">
</use>
</svg>
</div>
<p class="metric-value">800,000<small>원</small>
</p>
<p class="metric-foot">활성화된 고정지출</p>
</article>
<article class="panel metric sage">
<div class="metric-label">아직 예정인 지출<svg class="icon" aria-hidden="true">
<use href="#i-calendar">
</use>
</svg>
</div>
<p class="metric-value">220,000<small>원</small>
</p>
<p class="metric-foot">오늘 쓸 수 있는 돈에서 제외</p>
</article>
<article class="panel metric ">
<div class="metric-label">이미 기록된 지출<svg class="icon" aria-hidden="true">
<use href="#i-repeat">
</use>
</svg>
</div>
<p class="metric-value">580,000<small>원</small>
</p>
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
<table class="mobile-record-table data-table ">
<caption class="sr-only">고정 항목 목록</caption>
<thead>
<tr>
<th scope="col">항목 / 구분</th>
<th scope="col">금액</th>
<th scope="col">분류 / 결제수단</th>
<th scope="col">매월 결제일</th>
<th scope="col">휴일 보정</th>
<th scope="col">이번 주기</th>
<th scope="col">활성</th>
<th scope="col">관리</th>
</tr>
</thead>
<tbody>
<tr>
<th scope="row">월세 <span class="chip warm">지출</span>
</th>
<td data-label="금액">
<span class="expense">−550,000원</span>
</td>
<td data-label="분류 / 결제수단">고정지출 / 계좌이체</td>
<td data-label="매월 결제일">5일</td>
<td data-label="휴일 보정">그대로</td>
<td data-label="이번 주기">
<span class="chip">처리됨</span>
</td>
<td data-label="활성">
<label class="switch">
<input type="checkbox" name="rent-active" aria-label="월세 활성" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small edit-button" commandfor="recurring-edit-1" command="show-modal" aria-label="월세 수정">수정</button>
</td>
</tr>
<tr>
<th scope="row">통신 요금 <span class="chip warm">지출</span>
</th>
<td data-label="금액">
<span class="expense">−30,000원</span>
</td>
<td data-label="분류 / 결제수단">고정지출 / 신용카드</td>
<td data-label="매월 결제일">7일</td>
<td data-label="휴일 보정">그대로</td>
<td data-label="이번 주기">
<span class="chip">처리됨</span>
</td>
<td data-label="활성">
<label class="switch">
<input type="checkbox" name="phone-active" aria-label="통신 요금 활성" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small edit-button" commandfor="recurring-edit-2" command="show-modal" aria-label="통신 요금 수정">수정</button>
</td>
</tr>
<tr>
<th scope="row">음악 구독 <span class="chip warm">지출</span>
</th>
<td data-label="금액">
<span class="expense">−10,900원</span>
</td>
<td data-label="분류 / 결제수단">고정지출 / 신용카드</td>
<td data-label="매월 결제일">15일</td>
<td data-label="휴일 보정">그대로</td>
<td data-label="이번 주기">
<span class="chip warm">예정</span>
</td>
<td data-label="활성">
<label class="switch">
<input type="checkbox" name="music-active" aria-label="음악 구독 활성" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small edit-button" commandfor="recurring-edit-3" command="show-modal" aria-label="음악 구독 수정">수정</button>
</td>
</tr>
<tr>
<th scope="row">관리비 <span class="chip warm">지출</span>
</th>
<td data-label="금액">
<span class="expense">−209,100원</span>
</td>
<td data-label="분류 / 결제수단">고정지출 / 계좌이체</td>
<td data-label="매월 결제일">20일</td>
<td data-label="휴일 보정">직전 평일</td>
<td data-label="이번 주기">
<span class="chip warm">예정</span>
</td>
<td data-label="활성">
<label class="switch">
<input type="checkbox" name="housing-active" aria-label="관리비 활성" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small edit-button" commandfor="recurring-edit-4" command="show-modal" aria-label="관리비 수정">수정</button>
</td>
</tr>
<tr>
<th scope="row">월급 <span class="chip">수입</span>
</th>
<td data-label="금액">
<span class="income">+3,200,000원</span>
</td>
<td data-label="분류 / 결제수단">급여 / 계좌이체</td>
<td data-label="매월 결제일">26일</td>
<td data-label="휴일 보정">그대로</td>
<td data-label="이번 주기">
<span class="chip">처리됨</span>
</td>
<td data-label="활성">
<label class="switch">
<input type="checkbox" name="salary-active" aria-label="월급 활성" checked>
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small edit-button" commandfor="recurring-edit-5" command="show-modal" aria-label="월급 수정">수정</button>
</td>
</tr>
<tr>
<th scope="row">영상 구독 <span class="chip neutral">지출</span>
</th>
<td data-label="금액">−14,900원</td>
<td data-label="분류 / 결제수단">고정지출 / 신용카드</td>
<td data-label="매월 결제일">27일</td>
<td data-label="휴일 보정">그대로</td>
<td data-label="이번 주기">
<span class="chip neutral">비활성</span>
</td>
<td data-label="활성">
<label class="switch">
<input type="checkbox" name="video-active" aria-label="영상 구독 활성">
<span aria-hidden="true">
</span>
</label>
</td>
<td data-label="관리">
<button type="button" class="button small edit-button" commandfor="recurring-edit-6" command="show-modal" aria-label="영상 구독 수정">수정</button>
</td>
</tr>
</tbody>
</table>
</div>
</section>

<t:recurringEditor id="recurring-new" heading="고정 항목 등록" subtitle="필요한 내용을 입력해 주세요." name="" amount="" editing="false" kind="EXPENSE" category="고정지출" payment="신용카드" day="1" adjust="NONE" memo="" active="true"/>
<t:recurringEditor id="recurring-edit-1" heading="고정 항목 수정" subtitle="월세" editing="true" name="월세" amount="550000" kind="EXPENSE" category="고정지출" payment="계좌이체" day="5" adjust="NONE" memo="" active="true"/>
<t:recurringEditor id="recurring-edit-2" heading="고정 항목 수정" subtitle="통신 요금" editing="true" name="통신 요금" amount="30000" kind="EXPENSE" category="고정지출" payment="신용카드" day="7" adjust="NONE" memo="" active="true"/>
<t:recurringEditor id="recurring-edit-3" heading="고정 항목 수정" subtitle="음악 구독" editing="true" name="음악 구독" amount="10900" kind="EXPENSE" category="고정지출" payment="신용카드" day="15" adjust="NONE" memo="" active="true"/>
<t:recurringEditor id="recurring-edit-4" heading="고정 항목 수정" subtitle="관리비" editing="true" name="관리비" amount="209100" kind="EXPENSE" category="고정지출" payment="계좌이체" day="20" adjust="PREV_BIZ" memo="" active="true"/>
<t:recurringEditor id="recurring-edit-5" heading="고정 항목 수정" subtitle="월급" editing="true" name="월급" amount="3200000" kind="INCOME" category="급여" payment="계좌이체" day="26" adjust="NONE" memo="" active="true"/>
<t:recurringEditor id="recurring-edit-6" heading="고정 항목 수정" subtitle="영상 구독" editing="true" name="영상 구독" amount="14900" kind="EXPENSE" category="고정지출" payment="신용카드" day="27" adjust="NONE" memo="" active="false"/>
</jsp:body>
</t:layout>
