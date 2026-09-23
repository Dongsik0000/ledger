<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<t:layout title="UI 상태" page="">
<jsp:body>
<header class="page-head">
<div>
<p class="eyebrow">UI STATES</p>
<h1>공통 요소와 상태</h1>
<p class="page-description">알림, 오류, 확인, 로딩과 입력 상태의 정적 디자인 예시입니다.</p>
</div>
</header>
<div class="component-grid">
<section class="panel">
<h2>알림</h2>
<div class="manage-modal-dialog specimen-dialog">
<div class="manage-complete-body">
<span class="empty-icon">
<svg class="icon" aria-hidden="true">
<use href="#i-leaf">
</use>
</svg>
</span>
<h3 class="manage-complete-title">기록을 저장했어요</h3>
<p class="manage-complete-desc">오늘의 기록이 가계부에 담겼어요.</p>
<button type="button" class="manage-complete-button">확인</button>
</div>
</div>
</section>
<section class="panel">
<h2>오류</h2>
<div class="manage-modal-dialog specimen-dialog">
<div class="route-error-demo-body">
<div class="error-symbol">!</div>
<h3 class="common-warn-title">기록을 저장하지 못했어요</h3>
<p class="common-warn-desc">입력한 내용은 그대로 있어요.<br>잠시 후 다시 시도해 주세요.</p>
</div>
<div class="manage-modal-actions">
<button class="manage-modal-btn primary" type="button">확인</button>
</div>
</div>
<div class="inline-error" role="alert">아이디 또는 비밀번호를 확인해 주세요.</div>
</section>
<section class="panel">
<h2>확인</h2>
<div class="manage-modal-dialog specimen-dialog">
<div class="manage-modal-head">
<div>
<h3 class="common-confirm-title">이 기록을 삭제할까요?</h3>
<p class="common-confirm-desc">삭제한 기록은 되돌릴 수 없어요.</p>
</div>
</div>
<div class="manage-modal-actions">
<button class="manage-modal-btn" type="button">취소</button>
<button class="manage-modal-btn danger" type="button">삭제</button>
</div>
</div>
</section>
<section class="panel">
<h2>로딩 오버레이</h2>
<div class="loading-specimen">
<div aria-hidden="true" class="skeleton-lines">
<span>
</span>
<span>
</span>
<span>
</span>
</div>
<div class="line-manage-loading" role="status" aria-live="polite">
<div class="line-manage-loading-inner">
<div class="spinner-wrap">
<span class="krds-spinner">
</span>
</div>
<p class="loading-title">기록을 불러오는 중이에요</p>
</div>
</div>
</div>
</section>
<section class="panel">
<h2>금액 표시</h2>
<dl class="summary-lines">
<div>
<dt>수입 · 녹색과 +</dt>
<dd class="income">+3,200,000원</dd>
</div>
<div>
<dt>지출 · 적갈색과 −</dt>
<dd class="expense">−12,500원</dd>
</div>
<div>
<dt>양수 잔액 · 기본색</dt>
<dd>1,930,000원</dd>
</div>
<div>
<dt>음수 잔액 · 적갈색과 −</dt>
<dd class="expense">−30,000원</dd>
</div>
<div>
<dt>미조회 / 계산 불가</dt>
<dd>—</dd>
</div>
</dl>
<p class="section-note">예정 지출이 잔액보다 크면 일일 예산도 음수로 표시합니다. 임의로 0원으로 바꾸지 않습니다.</p>
</section>
<section class="panel">
<h2>버튼과 입력 상태</h2>
<div class="button-specimens">
<button type="button" class="button primary" >기록하기</button>
<button type="button" class="button " >취소</button>
<button type="button" class="button danger" >삭제</button>
<button type="button" class="button " disabled>처리 중</button>
</div>
<label class="field">금액<input type="text" inputmode="numeric" aria-invalid="true" aria-describedby="amount-error" value="0">
</label>
<p class="field-error" id="amount-error">금액은 1원 이상의 정수로 입력해 주세요.</p>
<label class="field">
<span>메모</span>
<input type="text" name="example-memo" placeholder="기억할 내용 (선택)">
</label>
</section>
</div>
<h2 class="section-subtitle">화면별 빈 상태</h2>
<div class="component-grid">
<section class="panel">
<h2>대시보드 · 빈 상태</h2>
<div class="empty-state">
<span class="empty-icon">
<svg class="icon" aria-hidden="true">
<use href="#i-book">
</use>
</svg>
</span>
<strong>오늘의 첫 기록을 남겨볼까요?</strong>
<p>작은 지출부터 하나씩 기록하면 나의 소비가 보이기 시작해요.</p>
</div>
<p class="form-note">예산을 계산할 수 없는 상태에서는 0원이 아닌 ‘—’로 표시합니다.</p>
</section>
<section class="panel">
<h2>거래 내역 · 빈 상태</h2>
<div class="empty-state">
<span class="empty-icon">
<svg class="icon" aria-hidden="true">
<use href="#i-book">
</use>
</svg>
</span>
<strong>이 달에는 아직 기록이 없어요</strong>
<p>첫 거래를 추가하고 나의 한 달을 시작해 보세요.</p>
</div>
<button type="button" class="button primary" >
<svg class="icon" aria-hidden="true">
<use href="#i-plus">
</use>
</svg>거래 추가</button>
</section>
<section class="panel">
<h2>요약 · 빈 상태</h2>
<div class="empty-state">
<span class="empty-icon">
<svg class="icon" aria-hidden="true">
<use href="#i-chart">
</use>
</svg>
</span>
<strong>이 기간에는 아직 기록이 없어요</strong>
<p>기간을 바꾸거나 거래를 추가하면 소비 흐름을 확인할 수 있어요.</p>
</div>
</section>
<section class="panel">
<h2>고정 항목 · 빈 상태</h2>
<div class="empty-state">
<span class="empty-icon">
<svg class="icon" aria-hidden="true">
<use href="#i-repeat">
</use>
</svg>
</span>
<strong>매달 챙기는 지출이 있나요?</strong>
<p>구독료, 공과금, 월급을 등록하고 반복되는 기록을 관리해 보세요.</p>
</div>
<button type="button" class="button primary" >첫 고정 항목 등록</button>
</section>
<section class="panel">
<h2>자산 · 빈 상태</h2>
<div class="empty-state">
<span class="empty-icon">
<svg class="icon" aria-hidden="true">
<use href="#i-wallet">
</use>
</svg>
</span>
<strong>나의 자산을 한곳에 모아보세요</strong>
<p>적금, 비상금, 주식 등 관리하고 싶은 자산을 추가해요.</p>
</div>
<button type="button" class="button primary" >첫 자산 추가</button>
</section>
</div>
</jsp:body>
</t:layout>
