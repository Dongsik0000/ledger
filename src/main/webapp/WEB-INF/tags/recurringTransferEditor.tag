<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ attribute name="id" required="true"%>
<%-- 정기 이체 추가·수정 창(화면에 하나). 값은 account.js 가 채우고 showModal() 로 연다 --%>
<dialog class="editor-dialog" id="${id}" aria-labelledby="${id}-heading">
    <header class="dialog-head">
        <div>
            <h2 id="${id}-heading">정기 이체 추가</h2>
            <p id="${id}-subtitle">매월 같은 날 옮기는 돈을 등록해요.</p>
        </div>
        <button type="button" class="icon-button" data-close aria-label="닫기">×</button>
    </header>
    <div class="dialog-body">
        <fieldset class="form-stack">
            <legend class="sr-only">정기 이체 입력</legend>
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
                    <span>매월 이체일</span>
                    <input type="number" id="${id}-day" min="1" max="31" inputmode="numeric">
                </label>
            </div>
            <fieldset class="choice-field">
                <legend>주말·공휴일인 경우</legend>
                <div class="choice-buttons">
                    <label><input type="radio" name="${id}-adjust" value="NONE" checked><span>그대로</span></label>
                    <label><input type="radio" name="${id}-adjust" value="PREV_BIZ"><span>직전 평일</span></label>
                    <label><input type="radio" name="${id}-adjust" value="NEXT_BIZ"><span>다음 평일</span></label>
                </div>
            </fieldset>
            <p class="form-note">월급날에 옮긴다면 월급 고정 항목과 같은 날짜·휴일 보정을 골라 주세요. 해당 날짜가 없는 달은 마지막 날을 기준으로 해요. 이번 달 이체일이 이미 지났다면 다음 이체일부터 기록해요.</p>
            <label class="field">
                <span>메모 (선택)</span>
                <input type="text" id="${id}-memo" maxlength="500" placeholder="예: 생활비">
            </label>
            <label class="checkbox-field">
                <input type="checkbox" id="${id}-active" checked>
                <span>자동 기록 활성화</span>
            </label>
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
