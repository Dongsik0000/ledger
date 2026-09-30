<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ attribute name="id" required="true"%>
<%-- 통장 추가·수정 창(화면에 하나). 값은 account.js 가 채우고 showModal() 로 연다 --%>
<dialog class="editor-dialog" id="${id}" aria-labelledby="${id}-heading">
    <header class="dialog-head">
        <div>
            <h2 id="${id}-heading">통장 추가</h2>
            <p id="${id}-subtitle">필요한 내용을 입력해 주세요.</p>
        </div>
        <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
    </header>
    <div class="dialog-body">
        <fieldset class="form-stack">
            <legend class="sr-only">통장 입력</legend>
            <label class="field">
                <span>이름</span>
                <input type="text" id="${id}-name" placeholder="예: 월급 통장, 생활비 통장" maxlength="50">
            </label>
            <label class="checkbox-field">
                <input type="checkbox" id="${id}-default">
                <span>기본 통장 (잔액을 가계부 잔액에서 자동 계산)</span>
            </label>
            <p class="form-note" data-default hidden>기본 통장 잔액 = 가계부 잔액 − 다른 통장 잔액이에요. 그래서 통장 합계가 항상 가계부 잔액과 같아요. 통장을 고르지 않은 거래는 이 통장에 들어가요. 은행 앱과 다르면 빠진 기록이 있다는 뜻이에요.</p>
            <div class="field-row" data-base>
                <label class="field">
                    <span>기준 잔액 (원)</span>
                    <input type="text" id="${id}-amount" inputmode="numeric" autocomplete="off" placeholder="0" class="amount-input">
                </label>
                <label class="field">
                    <span>기준일</span>
                    <input type="date" id="${id}-date">
                </label>
            </div>
            <p class="form-note" data-base>은행 앱에서 본 지금 잔액을 적어 주세요. 이미 기록한 거래는 이 잔액에 들어 있다고 보고, 이후 이 통장을 고른 거래와 이체를 더해 현재 잔액을 계산해요. 마이너스 통장은 앞에 -를 붙여 주세요.</p>
            <button type="button" class="button small" id="${id}-reset" data-base hidden>지금 잔액으로 다시 맞추기</button>
        </fieldset>
    </div>
    <footer class="dialog-footer">
        <div class="form-actions">
            <button type="button" class="button danger" id="${id}-delete" hidden>삭제</button>
            <button type="button" class="button" data-close>닫기</button>
            <button type="button" class="button primary" id="${id}-save">저장</button>
        </div>
    </footer>
</dialog>
