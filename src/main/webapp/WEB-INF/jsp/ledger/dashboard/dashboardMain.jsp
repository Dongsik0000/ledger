<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 오늘: 값은 dashboard.js 가 /ledger/dashboard/summary·recent·budgets, 이번 주는 /ledger/entry/list,
     다가오는 고정 항목은 /ledger/recurring/list, 선택지는 /ledger/settings/master 로 채운다 --%>
<t:layout title="오늘" page="dashboard">
    <jsp:attribute name="script">
        <script defer src="<c:url value='/resources/js/app/entry/entryForm.js'/>"></script>
        <script defer src="<c:url value='/resources/js/app/dashboard/dashboard.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <header class="page-head">
            <div>
                <p class="eyebrow" id="todayLabel">—</p>
                <h1>오늘</h1>
            </div>
            <button type="button" class="button primary" id="quickJump">
                <svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>기록하기</button>
        </header>
        <div class="today-grid">
            <section class="panel hero-card t-hero reveal" aria-labelledby="dailyLabel">
                <div class="mascot pop" id="mascot" aria-hidden="true"></div>
                <div>
                    <p class="allow-label" id="dailyLabel">오늘 쓸 수 있는 돈</p>
                    <p class="daily-budget"><span id="dailyBudget">—</span><span>원</span></p>
                    <p class="allow-sub"><span id="daysLeft">—</span> · 나갈 고정지출 <span id="pendingFixed">—</span>은 미리 뺐어요</p>
                </div>
                <p class="bubble" id="mascotLine" aria-live="polite">오늘의 기록을 살펴보고 있어요.</p>
                <div class="cycle-progress">
                    <div class="bar" aria-hidden="true"><span id="cycleBar" style="width:0"></span></div>
                    <div class="hero-foot">
                        <span>이번 주기 <strong id="cycleRange">—</strong></span>
                        <a class="text-link" href="<c:url value='/ledger/entry?period=CYCLE'/>">이번 주기 거래 보기<svg class="icon" aria-hidden="true"><use href="#i-right"></use></svg></a>
                    </div>
                </div>
            </section>
            <section class="panel t-sum reveal">
                <div class="panel-head">
                    <div>
                        <h2>이번 주기</h2>
                        <p>급여일부터 다음 급여일 전날까지</p>
                    </div>
                </div>
                <dl class="tiles">
                    <div class="tile"><dt>이월</dt><dd id="carryOver">—</dd></div>
                    <div class="tile"><dt>수입</dt><dd class="income" id="cycleIncome">—</dd></div>
                    <div class="tile"><dt>지출</dt><dd class="expense" id="cycleExpense">—</dd></div>
                    <div class="tile total"><dt>잔액</dt><dd id="cycleBalance">—</dd></div>
                </dl>
                <div class="total-line">
                    <span>나의 총 잔액</span>
                    <strong id="totalBalance">—</strong>
                    <small id="totalDetail">가계부 — + 자산 —</small>
                </div>
            </section>
            <section class="panel t-week reveal">
                <div class="panel-head">
                    <div>
                        <h2>이번 주</h2>
                        <p id="weekTotal">칸을 누르면 기록 달력에서 그날을 열어요</p>
                    </div>
                    <a class="button small" href="<c:url value='/ledger/entry'/>">달력 보기<svg class="icon" aria-hidden="true"><use href="#i-right"></use></svg></a>
                </div>
                <div class="week" id="weekStrip" role="group" aria-label="이번 주 날짜별 지출"></div>
            </section>
            <section class="panel t-bud reveal" id="limitPanel">
                <div class="panel-head">
                    <div>
                        <h2>카테고리 예산</h2>
                        <p>이번 주기 · 환불은 빼고 계산해요</p>
                    </div>
                    <a class="text-link" href="<c:url value='/ledger/settings#list-settings'/>">바꾸기<svg class="icon" aria-hidden="true"><use href="#i-right"></use></svg></a>
                </div>
                <ul class="limit-list t-body" id="limitList"></ul>
            </section>
            <section class="panel t-up reveal">
                <div class="panel-head">
                    <div>
                        <h2>다가오는 고정 항목</h2>
                        <p>결제일에 알아서 기록돼요</p>
                    </div>
                    <a class="text-link" href="<c:url value='/ledger/recurring'/>">관리<svg class="icon" aria-hidden="true"><use href="#i-right"></use></svg></a>
                </div>
                <div class="up-list t-body" id="upcomingList"></div>
            </section>
            <section class="panel t-rec reveal">
                <div class="panel-head">
                    <div>
                        <h2>최근 기록</h2>
                        <p>가장 최근 5건</p>
                    </div>
                    <a class="text-link" href="<c:url value='/ledger/entry?view=list'/>">전체 보기<svg class="icon" aria-hidden="true"><use href="#i-right"></use></svg></a>
                </div>
                <div class="t-body" id="recentList"></div>
            </section>
        </div>
        <details class="formula-note">
            <summary>오늘 쓸 수 있는 돈은 어떻게 계산하나요?</summary>
            <p id="formula">—</p>
            <p>달력의 월과 관계없이 오늘이 속한 주기를 기준으로 표시합니다.</p>
        </details>

        <%-- 빠른 기록 창: 머리의 "기록하기"·휴대폰 아래 + 로 연다. Enter 로 저장 --%>
        <dialog class="editor-dialog" id="quickDialog" aria-labelledby="quickDialog-heading">
            <form id="quickForm" novalidate>
                <header class="dialog-head">
                    <div>
                        <h2 id="quickDialog-heading">빠른 기록</h2>
                        <p>잊기 전에, 오늘의 기록을 남겨요. Enter 로 저장돼요.</p>
                    </div>
                    <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
                </header>
                <div class="dialog-body">
                    <fieldset class="form-stack">
                        <legend class="sr-only">거래 입력</legend>
                        <label class="field">
                            <span>금액</span>
                            <input type="text" id="quickAmount" inputmode="numeric" enterkeyhint="done" autocomplete="off" placeholder="0" class="amount-input" aria-describedby="quick-amount-help">
                        </label>
                        <p id="quick-amount-help" class="form-note">원 · 소수점 없이 입력해 주세요.</p>
                        <fieldset class="choice-field">
                            <legend>구분</legend>
                            <div class="choice-buttons">
                                <label><input type="radio" name="quickType" value="EXPENSE" checked><span>지출</span></label>
                                <label><input type="radio" name="quickType" value="INCOME"><span>수입</span></label>
                            </div>
                        </fieldset>
                        <label class="field">
                            <span>카테고리</span>
                            <select id="quickCategory"></select>
                        </label>
                        <p class="form-note" id="quickBudget" aria-live="polite" hidden></p>
                        <label class="field">
                            <span>내용</span>
                            <input type="text" id="quickTitle" enterkeyhint="done" placeholder="어디에 사용하셨나요?" maxlength="100">
                        </label>
                        <fieldset class="choice-field">
                            <legend>결제수단</legend>
                            <div class="choice-buttons" id="quickPayments"></div>
                        </fieldset>
                        <label class="field" hidden>
                            <span>통장</span>
                            <select id="quickAccount"></select>
                            <small class="form-note" id="quickBalance"></small>
                        </label>
                        <details class="optional-fields" id="quickOptional"><summary>날짜 · 메모 <span>기본 오늘</span></summary><div class="field-row">
                                <label class="field">
                                    <span>날짜</span>
                                    <input type="date" id="quickDate">
                                </label>
                                <label class="field">
                                    <span>메모 (선택)</span>
                                    <input type="text" id="quickMemo" placeholder="기억하고 싶은 한 줄" maxlength="500">
                                </label>
                        </div></details>
                    </fieldset>
                </div>
                <footer class="dialog-footer">
                    <div class="form-actions">
                        <button type="button" class="button" data-close>닫기</button>
                        <button type="submit" class="button primary" id="quickSave">
                            <svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>기록하기</button>
                    </div>
                </footer>
            </form>
        </dialog>
    </jsp:body>
</t:layout>
