<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 통장: 통장·정기 이체·최근 이체는 account.js 가 /ledger/account/list, 기본 통장 잔액은 /ledger/dashboard/summary 의 가계부 잔액으로 채운다 --%>
<t:layout title="통장" page="account">
    <jsp:attribute name="script">
        <script defer src="<c:url value='/resources/js/app/account/account.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <header class="page-head">
            <div>
                <p class="eyebrow">MY ACCOUNTS</p>
                <h1>통장</h1>
                <p class="page-description">통장마다 남은 돈과 통장 사이의 이체를 살펴봐요.</p>
            </div>
            <button type="button" class="button primary" id="accountNew">
                <svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>통장 추가</button>
        </header>
        <div class="three-metrics">
            <article class="panel metric featured">
                <div class="metric-label">통장 잔액 합계<svg class="icon" aria-hidden="true"><use href="#i-bank"></use></svg></div>
                <p class="metric-value"><span id="bankTotal">—</span><small>원</small></p>
                <p class="metric-foot">모든 통장의 현재 잔액</p>
            </article>
            <article class="panel metric sage">
                <div class="metric-label">가계부 잔액<svg class="icon" aria-hidden="true"><use href="#i-book"></use></svg></div>
                <p class="metric-value"><span id="ledgerBalance">—</span><small>원</small></p>
                <p class="metric-foot">대시보드 이번 주기 잔액</p>
            </article>
            <article class="panel metric">
                <div class="metric-label">차이<svg class="icon" aria-hidden="true"><use href="#i-info"></use></svg></div>
                <p class="metric-value"><span id="balanceDiff">—</span><small>원</small></p>
                <p class="metric-foot" id="balanceDiffFoot">통장 잔액 합계 − 가계부 잔액</p>
            </article>
        </div>
        <section class="panel">
            <div class="panel-head">
                <div>
                    <h2>나의 통장</h2>
                    <p>거래·고정 항목에서 고른 통장과 이체로 잔액을 계산해요.</p>
                </div>
            </div>
            <div class="table-scroll" tabindex="0" role="region" aria-label="통장 이름, 현재 잔액, 기준">
                <table class="mobile-record-table data-table">
                    <caption class="sr-only">통장 이름, 현재 잔액, 기준</caption>
                    <thead>
                        <tr>
                            <th scope="col">통장</th>
                            <th scope="col">현재 잔액</th>
                            <th scope="col">기준</th>
                            <th scope="col">관리</th>
                        </tr>
                    </thead>
                    <tbody id="accountBody"></tbody>
                </table>
            </div>
        </section>
        <section class="panel">
            <div class="panel-head">
                <div>
                    <h2>정기 이체</h2>
                    <p>매월 이체일에 통장 사이 이체를 자동으로 기록해요.</p>
                </div>
                <button type="button" class="button small" id="recurringNew">
                    <svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>정기 이체 추가</button>
            </div>
            <div class="table-scroll" tabindex="0" role="region" aria-label="정기 이체 목록">
                <table class="mobile-record-table data-table">
                    <caption class="sr-only">정기 이체 목록</caption>
                    <thead>
                        <tr>
                            <th scope="col">보내는 → 받는 통장</th>
                            <th scope="col">금액</th>
                            <th scope="col">매월 이체일</th>
                            <th scope="col">다음 이체일</th>
                            <th scope="col">관리</th>
                        </tr>
                    </thead>
                    <tbody id="recurringBody"></tbody>
                </table>
            </div>
        </section>
        <section class="panel">
            <div class="panel-head">
                <div>
                    <h2>최근 이체</h2>
                    <p>최근 100건까지 보여 줘요.</p>
                </div>
                <button type="button" class="button small" id="transferNew">
                    <svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>이체 기록</button>
            </div>
            <div class="table-scroll" tabindex="0" role="region" aria-label="최근 이체 목록">
                <table class="mobile-record-table data-table">
                    <caption class="sr-only">최근 이체 목록</caption>
                    <thead>
                        <tr>
                            <th scope="col">날짜</th>
                            <th scope="col">보내는 → 받는 통장</th>
                            <th scope="col">금액</th>
                            <th scope="col">메모</th>
                            <th scope="col">관리</th>
                        </tr>
                    </thead>
                    <tbody id="transferBody"></tbody>
                </table>
            </div>
        </section>
        <aside class="section-note">
            <strong>
                <svg class="icon" aria-hidden="true"><use href="#i-info"></use></svg>거래를 기록할 때 통장을 골라 주세요.</strong>결제수단은 체크카드·계좌이체 같은 결제 방식이고, 통장은 돈이 나가고 들어온 곳이에요. 통장을 고르지 않으면 기본 통장으로 들어가요. 신용카드 결제는 대금이 빠지는 통장을 고르면 쓴 날 바로 그 통장에서 빠져서, 결제일 전까지는 은행 앱보다 카드값만큼 적게 보여요. 이체는 수입·지출이 아니라서 가계부 잔액과 통계에는 영향을 주지 않아요.</aside>

        <t:accountEditor id="accountEditor"/>
        <t:transferEditor id="transferEditor"/>
        <t:recurringTransferEditor id="recurringTransferEditor"/>
    </jsp:body>
</t:layout>
