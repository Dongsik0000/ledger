<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/common/taglib.jsp"%>
<%-- 설정: 항목 카드를 누르면 그 항목만 창(dialog)으로 연다. 프로필 메뉴는 ?open=키 로 들어와 그 창을 바로 연다(settings.js).
     값은 settings.js 가 /ledger/settings/load 로 채운다. 공휴일은 holiday.js --%>
<t:layout title="설정" page="settings">
    <jsp:attribute name="script">
        <script defer src="<c:url value='/resources/js/app/settings/settings.js'/>"></script>
        <script defer src="<c:url value='/resources/js/app/settings/holiday.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <header class="page-head">
            <div>
                <p class="eyebrow">기록의 기준</p>
                <h1>설정</h1>
                <p class="page-description">바꾸고 싶은 항목을 누르면 작은 창에서 바로 고칠 수 있어요.</p>
            </div>
        </header>
        <section class="panel">
            <div class="panel-head">
                <div>
                    <h2>기록</h2>
                    <p>한 달의 기준과 시작 잔액, 내보내기</p>
                </div>
            </div>
            <div class="manage-cards">
                <button type="button" class="manage-card" data-open-dialog="cycleDialog" aria-haspopup="dialog">
                    <svg class="icon" aria-hidden="true"><use href="#i-calendar"></use></svg>
                    <strong>기록 주기</strong>
                    <span>월급날에 맞춰 한 달의 시작을 정해요.</span>
                </button>
                <button type="button" class="manage-card" data-open-dialog="openingDialog" aria-haspopup="dialog">
                    <svg class="icon" aria-hidden="true"><use href="#i-wallet"></use></svg>
                    <strong>시작 잔액</strong>
                    <span>기록을 시작한 날의 잔액부터 이월을 계산해요.</span>
                </button>
                <button type="button" class="manage-card" data-open-dialog="exportDialog" aria-haspopup="dialog">
                    <svg class="icon" aria-hidden="true"><use href="#i-download"></use></svg>
                    <strong>기록 내보내기</strong>
                    <span>기간을 골라 CSV 로 받아요. 엑셀에서 바로 열려요.</span>
                </button>
            </div>
        </section>
        <section class="panel">
            <div class="panel-head">
                <div>
                    <h2>목록</h2>
                    <p>거래에서 고르는 항목과 날짜 보정에 쓰는 공휴일</p>
                </div>
            </div>
            <div class="manage-cards">
                <button type="button" class="manage-card" id="categoryOpen" aria-haspopup="dialog">
                    <svg class="icon" aria-hidden="true"><use href="#i-bag"></use></svg>
                    <strong>카테고리 · 예산</strong>
                    <span>이름·그룹·순서·주기 예산을 바꾸거나 숨겨요.</span>
                </button>
                <button type="button" class="manage-card" id="paymentOpen" aria-haspopup="dialog">
                    <svg class="icon" aria-hidden="true"><use href="#i-wallet"></use></svg>
                    <strong>결제수단</strong>
                    <span>자주 쓰는 결제수단으로 정리해요.</span>
                </button>
                <button type="button" class="manage-card" id="holidayOpen" aria-haspopup="dialog">
                    <svg class="icon" aria-hidden="true"><use href="#i-calendar"></use></svg>
                    <strong>공휴일</strong>
                    <span>연도별로 가져오거나 직접 추가해요.</span>
                </button>
            </div>
        </section>
        <section class="panel">
            <div class="panel-head">
                <div>
                    <h2>계정</h2>
                    <p>비밀번호와 기록 정리</p>
                </div>
            </div>
            <div class="manage-cards">
                <button type="button" class="manage-card" data-open-dialog="passwordDialog" aria-haspopup="dialog">
                    <svg class="icon" aria-hidden="true"><use href="#i-shield"></use></svg>
                    <strong>비밀번호 변경</strong>
                    <span>바꾼 뒤에도 지금 로그인은 유지돼요.</span>
                </button>
                <button type="button" class="manage-card" data-open-dialog="dangerDialog" aria-haspopup="dialog">
                    <svg class="icon" aria-hidden="true"><use href="#i-info"></use></svg>
                    <strong>초기화 · 탈퇴</strong>
                    <span>되돌릴 수 없어요. 먼저 기록을 내보내 두세요.</span>
                </button>
            </div>
        </section>

        <dialog class="editor-dialog" id="cycleDialog" aria-labelledby="cycleDialog-heading">
            <header class="dialog-head">
                <div>
                    <h2 id="cycleDialog-heading">기록 주기</h2>
                    <p>월급날에 맞춰 한 달의 시작을 정해요. 1일이면 달력 월과 같아요.</p>
                </div>
                <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
            </header>
            <div class="dialog-body">
                <div class="setting-row">
                    <label for="payDay"><strong>주기 시작일</strong></label>
                    <div class="payday-select">
                        <span aria-hidden="true">매월</span>
                        <select id="payDay" name="payDay">
                            <c:forEach var="d" begin="1" end="31"><option value="${d}">${d}일</option></c:forEach>
                        </select>
                    </div>
                </div>
                <div class="setting-row">
                    <div>
                        <strong>주말·공휴일이면 직전 평일로</strong>
                        <p>시작일이 쉬는 날이면 하루씩 앞당겨요.</p>
                    </div>
                    <label class="switch">
                        <input type="checkbox" id="payDayAdjust" name="payDayAdjust" aria-label="주말 공휴일 직전 평일 보정">
                        <span aria-hidden="true"></span>
                    </label>
                </div>
                <div class="cycle-ruler" id="cycleRuler">
                    <div class="cycle-ruler-ends">
                        <span>이번 주기</span>
                        <strong id="cycleRange">불러오는 중</strong>
                    </div>
                    <div class="cycle-track" aria-hidden="true"><span id="cycleFill"></span></div>
                    <p class="cycle-caption" id="cycleCaption" role="status"></p>
                </div>
            </div>
            <footer class="dialog-footer">
                <div class="form-actions">
                    <button type="button" class="button" data-close>닫기</button>
                    <button type="button" class="button primary" id="cycleSave">주기 저장</button>
                </div>
            </footer>
        </dialog>

        <dialog class="editor-dialog" id="openingDialog" aria-labelledby="openingDialog-heading">
            <header class="dialog-head">
                <div>
                    <h2 id="openingDialog-heading">시작 잔액</h2>
                    <p>기록을 시작한 날의 잔액부터 이월을 계산해요. 그 전 거래는 통계에만 남아요.</p>
                </div>
                <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
            </header>
            <div class="dialog-body">
                <div class="form-stack">
                    <label class="field">
                        <span>잔액 (원)</span>
                        <input type="text" id="openingBalance" autocomplete="off" placeholder="0" class="amount-input">
                    </label>
                    <label class="field">
                        <span>기준일</span>
                        <input type="date" id="openingDate">
                    </label>
                    <p class="form-note">마이너스 잔액은 앞에 -를 붙여요. 기준일을 비우고 저장하면 시작 잔액을 쓰지 않아요.</p>
                </div>
            </div>
            <footer class="dialog-footer">
                <div class="form-actions">
                    <button type="button" class="button" data-close>닫기</button>
                    <button type="button" class="button primary" id="openingSave">시작 잔액 저장</button>
                </div>
            </footer>
        </dialog>

        <dialog class="editor-dialog" id="exportDialog" aria-labelledby="exportDialog-heading">
            <header class="dialog-head">
                <div>
                    <h2 id="exportDialog-heading">기록 내보내기</h2>
                    <p>날짜, 구분, 카테고리, 내용, 금액, 결제수단, 메모를 CSV로 받아요.</p>
                </div>
                <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
            </header>
            <div class="dialog-body">
                <fieldset class="choice-field">
                    <legend>기간</legend>
                    <div class="choice-buttons" id="exportPresets">
                        <label><input type="radio" name="exportRange" value="cycle"><span>이번 주기</span></label>
                        <label><input type="radio" name="exportRange" value="month"><span>이번 달</span></label>
                        <label><input type="radio" name="exportRange" value="lastMonth"><span>지난달</span></label>
                        <label><input type="radio" name="exportRange" value="year"><span>올해</span></label>
                    </div>
                </fieldset>
                <div class="field-row export-range">
                    <label class="field">
                        <span>시작일</span>
                        <input type="date" id="exportFrom" name="exportFrom">
                    </label>
                    <label class="field">
                        <span>종료일</span>
                        <input type="date" id="exportTo" name="exportTo">
                    </label>
                </div>
            </div>
            <footer class="dialog-footer">
                <div class="form-actions">
                    <button type="button" class="button" data-close>닫기</button>
                    <button type="button" class="button primary" id="exportButton">
                        <svg class="icon" aria-hidden="true"><use href="#i-download"></use></svg>CSV 내보내기</button>
                </div>
            </footer>
        </dialog>

        <dialog class="editor-dialog" id="passwordDialog" aria-labelledby="passwordDialog-heading">
            <form id="passwordForm" novalidate>
                <header class="dialog-head">
                    <div>
                        <h2 id="passwordDialog-heading">비밀번호 변경</h2>
                        <p>바꾼 뒤에도 지금 로그인은 유지돼요.</p>
                    </div>
                    <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
                </header>
                <div class="dialog-body">
                    <div class="form-stack">
                        <%-- 비밀번호 관리자가 어느 계정의 비밀번호인지 알 수 있게(화면에는 보이지 않음) --%>
                        <input type="text" name="username" autocomplete="username" value="<c:out value='${sessionScope.username}'/>" hidden>
                        <label class="field">
                            <span>현재 비밀번호</span>
                            <input type="password" id="currentPassword" autocomplete="current-password">
                        </label>
                        <label class="field">
                            <span>새 비밀번호 · 8자 이상</span>
                            <input type="password" id="newPassword" autocomplete="new-password" minlength="8">
                        </label>
                        <label class="field">
                            <span>새 비밀번호 확인</span>
                            <input type="password" id="newPasswordConfirm" autocomplete="new-password" minlength="8">
                        </label>
                    </div>
                </div>
                <footer class="dialog-footer">
                    <div class="form-actions">
                        <button type="button" class="button" data-close>닫기</button>
                        <button type="submit" class="button primary">비밀번호 변경</button>
                    </div>
                </footer>
            </form>
        </dialog>

        <%-- 되돌릴 수 없는 작업: 현재 비밀번호를 받고 한 번 더 물은 뒤 보낸다(settings.js dangerAction) --%>
        <dialog class="editor-dialog" id="dangerDialog" aria-labelledby="dangerDialog-heading">
            <form id="dangerForm" novalidate>
                <header class="dialog-head">
                    <div>
                        <h2 id="dangerDialog-heading">기록 초기화 · 계정 탈퇴</h2>
                        <p>되돌릴 수 없어요. 먼저 ‘기록 내보내기’로 CSV 를 받아 두세요.</p>
                    </div>
                    <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
                </header>
                <div class="dialog-body">
                    <div class="form-stack">
                        <input type="text" name="username" autocomplete="username" value="<c:out value='${sessionScope.username}'/>" hidden>
                        <label class="field">
                            <span>현재 비밀번호</span>
                            <input type="password" id="dangerPassword" autocomplete="current-password">
                        </label>
                        <ul class="form-note danger-list">
                            <li><strong>기록 초기화</strong>: 거래 · 이체 · 고정 항목 · 통장 · 자산 · 시작 잔액을 지워요. 카테고리 · 결제수단 · 예산 · 주기 설정은 남아요.</li>
                            <li><strong>계정 탈퇴</strong>: 계정과 모든 기록을 지우고 로그아웃해요. 서버 백업에는 최대 30일 동안 남은 뒤 지워져요.</li>
                        </ul>
                    </div>
                </div>
                <footer class="dialog-footer">
                    <div class="form-actions">
                        <button type="button" class="button" data-close>닫기</button>
                        <button type="button" class="button danger" id="resetData">기록 초기화</button>
                        <button type="button" class="button danger" id="withdraw">계정 탈퇴</button>
                    </div>
                </footer>
            </form>
        </dialog>

        <dialog class="editor-dialog wide" id="categoryDialog" aria-labelledby="categoryDialog-heading">
            <header class="dialog-head">
                <div>
                    <h2 id="categoryDialog-heading">카테고리 관리</h2>
                    <p>손잡이를 끌어 순서를 바꿔요. 고친 줄은 저절로 선택되고, 아래에서 한 번에 저장해요.</p>
                </div>
                <div class="dialog-tools">
                    <button type="button" class="button small primary" id="categoryAddOpen" aria-expanded="false" aria-controls="categoryAdd">
                        <svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>추가</button>
                    <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
                </div>
            </header>
            <div class="dialog-body">
                <section class="add-panel" id="categoryAdd" aria-labelledby="categoryAdd-heading" hidden>
                    <h3 class="section-subtitle" id="categoryAdd-heading">새 카테고리</h3>
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
                        <div class="form-actions">
                            <button type="button" class="button" id="categoryAddCancel">취소</button>
                            <button type="button" class="button primary" id="categoryAddSave">저장</button>
                        </div>
                    </div>
                </section>
                <h3 class="section-subtitle" id="expenseCategoryTitle">지출 카테고리</h3>
                <ul class="sort-list" id="expenseCategoryList" data-type="EXPENSE" aria-labelledby="expenseCategoryTitle"></ul>
                <h3 class="section-subtitle" id="incomeCategoryTitle">수입 카테고리</h3>
                <ul class="sort-list" id="incomeCategoryList" data-type="INCOME" aria-labelledby="incomeCategoryTitle"></ul>
                <p class="sort-status" id="categoryOrderStatus" role="status"></p>
            </div>
            <%-- 고친 줄은 저절로 선택된다. 선택한 줄만 한 번에 저장하거나 삭제(사용 중이면 숨김) --%>
            <footer class="dialog-footer bulk-bar">
                <label class="check-field"><input type="checkbox" id="categorySelectAll"><span>전체 선택</span></label>
                <span class="bulk-count" id="categorySelectCount" role="status">선택 0개</span>
                <div class="bulk-actions">
                    <button type="button" class="button danger" id="categoryBulkDelete" disabled>선택 삭제</button>
                    <button type="button" class="button primary" id="categoryBulkSave" disabled>선택 저장</button>
                </div>
            </footer>
        </dialog>
        <dialog class="editor-dialog wide" id="paymentDialog" aria-labelledby="paymentDialog-heading">
            <header class="dialog-head">
                <div>
                    <h2 id="paymentDialog-heading">결제수단 관리</h2>
                    <p>자주 쓰는 결제수단을 위로 끌어 올려 두세요. 고친 줄은 아래에서 한 번에 저장해요.</p>
                </div>
                <div class="dialog-tools">
                    <button type="button" class="button small primary" id="paymentAddOpen" aria-expanded="false" aria-controls="paymentAdd">
                        <svg class="icon" aria-hidden="true"><use href="#i-plus"></use></svg>추가</button>
                    <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
                </div>
            </header>
            <div class="dialog-body">
                <section class="add-panel" id="paymentAdd" aria-labelledby="paymentAdd-heading" hidden>
                    <h3 class="section-subtitle" id="paymentAdd-heading">새 결제수단</h3>
                    <label class="field">
                        <span>이름</span>
                        <input type="text" id="newPaymentName" placeholder="예: 생활비 카드" maxlength="50">
                    </label>
                    <div class="form-actions">
                        <button type="button" class="button" id="paymentAddCancel">취소</button>
                        <button type="button" class="button primary" id="paymentAddSave">저장</button>
                    </div>
                </section>
                <ul class="sort-list" id="paymentList" aria-label="결제수단"></ul>
                <p class="sort-status" id="paymentOrderStatus" role="status"></p>
            </div>
            <footer class="dialog-footer bulk-bar">
                <label class="check-field"><input type="checkbox" id="paymentSelectAll"><span>전체 선택</span></label>
                <span class="bulk-count" id="paymentSelectCount" role="status">선택 0개</span>
                <div class="bulk-actions">
                    <button type="button" class="button danger" id="paymentBulkDelete" disabled>선택 삭제</button>
                    <button type="button" class="button primary" id="paymentBulkSave" disabled>선택 저장</button>
                </div>
            </footer>
        </dialog>
        <dialog class="editor-dialog wide" id="holidayDialog" aria-labelledby="holidayDialog-heading">
            <header class="dialog-head">
                <div>
                    <h2 id="holidayDialog-heading">공휴일 관리</h2>
                    <p>주기 시작일과 고정 항목의 날짜 보정에 사용해요. 모든 사용자가 함께 써요.</p>
                </div>
                <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
            </header>
            <div class="dialog-body">
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
            </div>
        </dialog>
    </jsp:body>
</t:layout>
