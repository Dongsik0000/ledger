<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ attribute name="id" required="true"%>
<%-- 고정 항목 등록·수정 창(화면에 하나). 값은 recurring.js 가 채우고 showModal() 로 연다 --%>
<dialog class="editor-dialog" id="${id}" aria-labelledby="${id}-heading">
<header class="dialog-head">
<div>
<h2 id="${id}-heading">고정 항목 등록</h2>
<p id="${id}-subtitle">필요한 내용을 입력해 주세요.</p>
</div>
<button type="button" class="icon-button" data-close aria-label="닫기">×</button>
</header>
<div class="dialog-body">
<fieldset class="form-stack">
<legend class="sr-only">고정 항목 입력</legend>
<label class="field">
<span>항목명</span>
<input type="text" id="${id}-name" placeholder="예: 월세, 구독료, 월급" maxlength="100">
</label>
<fieldset class="choice-field">
<legend>구분</legend>
<div class="choice-buttons">
<label><input type="radio" name="${id}-type" value="EXPENSE" checked><span>지출</span></label>
<label><input type="radio" name="${id}-type" value="INCOME"><span>수입</span></label>
</div>
</fieldset>
<div class="field-row">
<label class="field">
<span>금액 (원)</span>
<input type="text" id="${id}-amount" inputmode="numeric" autocomplete="off" placeholder="0" class="amount-input">
</label>
<label class="field">
<span>매월 결제일</span>
<input type="number" id="${id}-day" min="1" max="31" inputmode="numeric">
</label>
</div>
<div class="field-row">
<label class="field">
<span>카테고리</span>
<select id="${id}-category"></select>
</label>
<label class="field">
<span>결제수단</span>
<select id="${id}-payment"></select>
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
<p class="form-note">해당 날짜가 없는 달은 그 달의 마지막 날을 기준으로 해요. 이번 달 결제일이 이미 지났다면 이번 달은 건너뛰고 다음 결제일부터 기록해요.</p>
<label class="field">
<span>메모 (선택)</span>
<input type="text" id="${id}-memo" maxlength="500">
</label>
<label class="checkbox-field">
<input type="checkbox" id="${id}-active" checked>
<span>자동 기록 활성화</span>
</label>
<p class="form-note">끄면 이후 자동 기록에서 제외돼요. 이전 거래는 남아 있어요.</p>
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
