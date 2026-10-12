<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 거래 내역: 목록·필터 선택지는 entry.js 가 채운다 --%>
<t:layout title="거래 내역" page="entry">
    <jsp:attribute name="script">
        <script defer src="<c:url value='/resources/js/app/entry/entryForm.js'/>"></script>
        <script defer src="<c:url value='/resources/js/app/entry/entryBatch.js'/>"></script>
        <script defer src="<c:url value='/resources/js/app/entry/entry.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <header class="page-head">
            <div>
                <p class="eyebrow">MY TRANSACTIONS</p>
                <h1>거래 내역</h1>
                <p class="page-description">언제, 어디에 썼는지 차분히 돌아봐요.</p>
            </div>
            <div class="page-actions">
                <button type="button" class="button" id="entryBatchOpen">여러 건</button>
                <button type="button" class="button primary" id="entryNew">
                    <svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>거래 추가</button>
            </div>
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
                            <option value="CYCLE">주기</option>
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

        <%-- 여러 건 입력 창. 줄은 entryBatch.js 가 만든다 --%>
        <dialog class="editor-dialog wide" id="entryBatch" aria-labelledby="entryBatch-heading">
            <header class="dialog-head">
                <div>
                    <h2 id="entryBatch-heading">여러 건 입력</h2>
                    <p>줄마다 그 통장의 거래 후 잔액이 이어서 보여요. 은행 앱 거래내역과 한 줄씩 맞춰 보세요.</p>
                </div>
                <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
            </header>
            <div class="dialog-body">
                <div class="table-scroll" tabindex="0" role="region" aria-label="입력할 거래">
                    <table class="mobile-record-table data-table batch-table">
                        <caption class="sr-only">입력할 거래</caption>
                        <thead>
                            <tr>
                                <th scope="col">번호</th>
                                <th scope="col">날짜</th>
                                <th scope="col">구분</th>
                                <th scope="col">카테고리</th>
                                <th scope="col">내용</th>
                                <th scope="col">금액 (원)</th>
                                <th scope="col">결제수단</th>
                                <th scope="col">통장</th>
                                <th scope="col">거래 후 잔액</th>
                                <th scope="col">관리</th>
                            </tr>
                        </thead>
                        <tbody id="entryBatch-body"></tbody>
                    </table>
                </div>
                <button type="button" class="button small batch-add" id="entryBatch-add">
                    <svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>줄 추가</button>
                <p class="form-note">새 줄은 윗줄의 날짜·구분·카테고리·결제수단·통장을 이어받아요. 마지막 줄에서 Enter 를 누르면 줄이 추가돼요. 비워 둔 줄은 저장하지 않아요.</p>
            </div>
            <footer class="dialog-footer">
                <div class="batch-summary" id="entryBatch-summary"></div>
                <div class="form-actions">
                    <button type="button" class="button" data-close>닫기</button>
                    <button type="button" class="button primary" id="entryBatch-save">저장</button>
                </div>
            </footer>
        </dialog>
    </jsp:body>
</t:layout>
