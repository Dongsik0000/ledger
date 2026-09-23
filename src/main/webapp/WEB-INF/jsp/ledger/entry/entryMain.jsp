<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 정적 UI 예시. 신규 이벤트·계산·저장 로직 없음. 금액과 날짜는 디자인 확인용. --%>
<jsp:useBean id="uiToday" class="java.util.Date"/>
<fmt:formatDate value="${uiToday}" pattern="yyyy-MM-dd" var="uiDate"/>
<t:layout title="거래 내역" page="entry">
<jsp:body>
<header class="page-head">
<div>
<p class="eyebrow">MY TRANSACTIONS</p>
<h1>거래 내역</h1>
<p class="page-description">언제, 어디에 썼는지 차분히 돌아봐요.</p>
</div>
<button type="button" class="button primary" commandfor="entry-new" command="show-modal">
<svg class="icon" aria-hidden="true">
<use href="#i-plus">
</use>
</svg>거래 추가</button>
</header>
<section class="panel reveal">
<div class="panel-head">
<div>
<h2>2026년 10월</h2>
<p>달력 월 기준 · 10월 1일~31일</p>
</div>
<div class="period-control">
<button type="button" class="button " aria-label="이전 달">
<svg class="icon" aria-hidden="true">
<use href="#i-left">
</use>
</svg>
</button>
<label class="field">
<span>조회 월</span>
<input type="month" name="month" value="2026-10" >
</label>
<button type="button" class="button " aria-label="다음 달">
<svg class="icon" aria-hidden="true">
<use href="#i-right">
</use>
</svg>
</button>
</div>
</div>
<details class="filter-disclosure"><summary>검색 · 필터 <span>수입·지출 / 분류</span></summary><div class="filter-body"><div class="filters">
<label class="field">
<span>검색</span>
<input type="search" name="keyword" placeholder="내용이나 메모 검색">
</label>
<label class="field">
<span>구분</span>
<select name="type">
<option>전체</option>
<option>지출</option>
<option>수입</option>
</select>
</label>
<label class="field">
<span>카테고리</span>
<select name="category">
<option>전체</option>
<option>식비</option>
<option>배달</option>
<option>카페/간식</option>
<option>장보기</option>
<option>교통</option>
<option>고정지출</option>
<option>저축</option>
<option>기타</option>
<option>급여</option>
</select>
</label>
<label class="field">
<span>결제수단</span>
<select name="payment">
<option>전체</option>
<option>신용카드</option>
<option>체크카드</option>
<option>계좌이체</option>
<option>현금</option>
</select>
</label>
</div>
<div class="filter-actions">
<button type="button" class="button " >초기화</button>
<button type="button" class="button primary" >
<svg class="icon" aria-hidden="true">
<use href="#i-search">
</use>
</svg>조회</button>
</div>
</div></details>
<div class="list-heading">
<span>최근 거래 예시 10건</span>
<span>수입 <b class="income">+</b> · 지출 <b class="expense">−</b>
</span>
</div>
<div class="date-label">
<time datetime="2026-10-08">10월 8일</time>
<span>일 합계 <strong class="expense">−17,300원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon warm">
<svg class="icon" aria-hidden="true">
<use href="#i-coffee">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>동네 카페</strong>
<small>카페/간식 · 체크카드</small>
<span class="memo-tag" title="따뜻한 라테">
<svg class="icon" aria-hidden="true">
<use href="#i-edit">
</use>
</svg>
<span>따뜻한 라테</span>
</span>
</div>
<strong class="amount expense">−4,800원</strong>
<button type="button" class="button small edit-button" commandfor="entry-edit-1" command="show-modal" aria-label="동네 카페 수정">수정</button>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-food">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>오늘의 점심</strong>
<small>식비 · 신용카드</small>
</div>
<strong class="amount expense">−12,500원</strong>
<button type="button" class="button small edit-button" commandfor="entry-edit-2" command="show-modal" aria-label="오늘의 점심 수정">수정</button>
</div>
<div class="date-label">
<time datetime="2026-10-07">10월 7일</time>
<span>일 합계 <strong class="expense">−46,700원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-bag">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>주말 장보기</strong>
<small>장보기 · 체크카드</small>
<span class="memo-tag" title="과일과 채소">
<svg class="icon" aria-hidden="true">
<use href="#i-edit">
</use>
</svg>
<span>과일과 채소</span>
</span>
</div>
<strong class="amount expense">−45,200원</strong>
<button type="button" class="button small edit-button" commandfor="entry-edit-3" command="show-modal" aria-label="주말 장보기 수정">수정</button>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-train">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>지하철</strong>
<small>교통 · 체크카드</small>
</div>
<strong class="amount expense">−1,500원</strong>
<button type="button" class="button small edit-button" commandfor="entry-edit-4" command="show-modal" aria-label="지하철 수정">수정</button>
</div>
<div class="date-label">
<time datetime="2026-10-06">10월 6일</time>
<span>일 합계 <strong class="expense">−218,000원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-book">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>읽고 싶었던 책</strong>
<small>기타 · 신용카드</small>
</div>
<strong class="amount expense">−18,000원</strong>
<button type="button" class="button small edit-button" commandfor="entry-edit-5" command="show-modal" aria-label="읽고 싶었던 책 수정">수정</button>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-leaf">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>차곡차곡 적금</strong>
<small>저축 · 계좌이체</small>
<span class="memo-tag" title="10월 저축">
<svg class="icon" aria-hidden="true">
<use href="#i-edit">
</use>
</svg>
<span>10월 저축</span>
</span>
</div>
<strong class="amount expense">−200,000원</strong>
<button type="button" class="button small edit-button" commandfor="entry-edit-6" command="show-modal" aria-label="차곡차곡 적금 수정">수정</button>
</div>
<div class="date-label">
<time datetime="2026-10-05">10월 5일</time>
<span>일 합계 <strong class="expense">−582,000원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-food">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>친구와 저녁</strong>
<small>식비 · 신용카드</small>
</div>
<strong class="amount expense">−32,000원</strong>
<button type="button" class="button small edit-button" commandfor="entry-edit-7" command="show-modal" aria-label="친구와 저녁 수정">수정</button>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-home">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>월세</strong>
<small>고정지출 · 계좌이체</small>
</div>
<strong class="amount expense">−550,000원</strong>
<button type="button" class="button small edit-button" commandfor="entry-edit-8" command="show-modal" aria-label="월세 수정">수정</button>
</div>
<div class="date-label">
<time datetime="2026-10-03">10월 3일</time>
<span>일 합계 <strong class="expense">−4,500원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon warm">
<svg class="icon" aria-hidden="true">
<use href="#i-coffee">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>오후의 커피</strong>
<small>카페/간식 · 현금</small>
</div>
<strong class="amount expense">−4,500원</strong>
<button type="button" class="button small edit-button" commandfor="entry-edit-9" command="show-modal" aria-label="오후의 커피 수정">수정</button>
</div>
<div class="date-label">
<time datetime="2026-10-01">10월 1일</time>
<span>일 합계 <strong class="income">+3,200,000원</strong>
</span>
</div>
<div class="transaction">
<span class="category-icon ">
<svg class="icon" aria-hidden="true">
<use href="#i-wallet">
</use>
</svg>
</span>
<div class="transaction-info">
<strong>월급</strong>
<small>급여 · 계좌이체</small>
</div>
<strong class="amount income">+3,200,000원</strong>
<button type="button" class="button small edit-button" commandfor="entry-edit-10" command="show-modal" aria-label="월급 수정">수정</button>
</div>
</section>

<t:entryEditor id="entry-new" heading="거래 추가" subtitle="필요한 내용을 입력해 주세요." name="" amount="" editing="false" kind="EXPENSE" category="식비" payment="체크카드" date="${uiDate}" memo=""/>
<t:entryEditor id="entry-edit-1" heading="거래 수정" subtitle="선택한 거래의 내용을 확인해 주세요." editing="true" name="동네 카페" amount="4,800" category="카페/간식" payment="체크카드" kind="EXPENSE" date="2026-10-08" memo="따뜻한 라테"/>
<t:entryEditor id="entry-edit-2" heading="거래 수정" subtitle="선택한 거래의 내용을 확인해 주세요." editing="true" name="오늘의 점심" amount="12,500" category="식비" payment="신용카드" kind="EXPENSE" date="2026-10-08" memo=""/>
<t:entryEditor id="entry-edit-3" heading="거래 수정" subtitle="선택한 거래의 내용을 확인해 주세요." editing="true" name="주말 장보기" amount="45,200" category="장보기" payment="체크카드" kind="EXPENSE" date="2026-10-07" memo="과일과 채소"/>
<t:entryEditor id="entry-edit-4" heading="거래 수정" subtitle="선택한 거래의 내용을 확인해 주세요." editing="true" name="지하철" amount="1,500" category="교통" payment="체크카드" kind="EXPENSE" date="2026-10-07" memo=""/>
<t:entryEditor id="entry-edit-5" heading="거래 수정" subtitle="선택한 거래의 내용을 확인해 주세요." editing="true" name="읽고 싶었던 책" amount="18,000" category="기타" payment="신용카드" kind="EXPENSE" date="2026-10-06" memo=""/>
<t:entryEditor id="entry-edit-6" heading="거래 수정" subtitle="선택한 거래의 내용을 확인해 주세요." editing="true" name="차곡차곡 적금" amount="200,000" category="저축" payment="계좌이체" kind="EXPENSE" date="2026-10-06" memo="10월 저축"/>
<t:entryEditor id="entry-edit-7" heading="거래 수정" subtitle="선택한 거래의 내용을 확인해 주세요." editing="true" name="친구와 저녁" amount="32,000" category="식비" payment="신용카드" kind="EXPENSE" date="2026-10-05" memo=""/>
<t:entryEditor id="entry-edit-8" heading="거래 수정" subtitle="선택한 거래의 내용을 확인해 주세요." editing="true" name="월세" amount="550,000" category="고정지출" payment="계좌이체" kind="EXPENSE" date="2026-10-05" memo=""/>
<t:entryEditor id="entry-edit-9" heading="거래 수정" subtitle="선택한 거래의 내용을 확인해 주세요." editing="true" name="오후의 커피" amount="4,500" category="카페/간식" payment="현금" kind="EXPENSE" date="2026-10-03" memo=""/>
<t:entryEditor id="entry-edit-10" heading="거래 수정" subtitle="선택한 거래의 내용을 확인해 주세요." editing="true" name="월급" amount="3,200,000" category="급여" payment="계좌이체" kind="INCOME" date="2026-10-01" memo=""/>
</jsp:body>
</t:layout>
