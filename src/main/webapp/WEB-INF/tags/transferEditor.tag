<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ attribute name="id" required="true"%>
<%-- 이체 기록·수정 창(화면에 하나). 값은 account.js 가 채우고 showModal() 로 연다 --%>
<dialog class="editor-dialog" id="${id}" aria-labelledby="${id}-heading">
    <header class="dialog-head">
        <div>
            <h2 id="${id}-heading">이체 기록</h2>
            <p id="${id}-subtitle">내 통장 사이에 옮긴 돈을 기록해요.</p>
        </div>
        <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
    </header>
    <div class="dialog-body">
        <fieldset class="form-stack">
            <legend class="sr-only">이체 입력</legend>
            <div class="field-row">
                <label class="field">
                    <span>보내는 통장</span>
                    <select id="${id}-from"></select>
                </label>
                <label class="field">
                    <span>받는 통장</span>
                    <select id="${id}-to"></select>
                </label>
            </div>
            <div class="field-row">
                <label class="field">
                    <span>금액 (원)</span>
                    <input type="text" id="${id}-amount" inputmode="numeric" autocomplete="off" placeholder="0" class="amount-input">
                </label>
                <label class="field">
                    <span>날짜</span>
                    <input type="date" id="${id}-date">
                </label>
            </div>
            <label class="field">
                <span>메모 (선택)</span>
                <input type="text" id="${id}-memo" maxlength="500">
            </label>
            <p class="form-note">통장의 기준일보다 앞선 이체는 그 통장 잔액에 들어가지 않아요.</p>
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
