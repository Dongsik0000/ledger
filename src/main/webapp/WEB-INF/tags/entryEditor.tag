<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ attribute name="id" required="true"%>
<%-- 거래 추가·수정 창(화면에 하나). 값은 entry.js 가 채우고 showModal() 로 연다 --%>
<dialog class="editor-dialog" id="${id}" aria-labelledby="${id}-heading">
<header class="dialog-head">
<div>
<h2 id="${id}-heading">거래 추가</h2>
<p id="${id}-subtitle">필요한 내용을 입력해 주세요.</p>
</div>
<button type="button" class="icon-button" data-close aria-label="닫기">×</button>
</header>
<div class="dialog-body">
<fieldset class="form-stack">
<legend class="sr-only">거래 입력</legend>
<label class="field">
<span>금액 (원)</span>
<input type="text" id="${id}-amount" inputmode="numeric" autocomplete="off" placeholder="0" class="amount-input">
</label>
<fieldset class="choice-field">
<legend>구분</legend>
<div class="choice-buttons">
<label><input type="radio" name="${id}-type" value="EXPENSE" checked><span>지출</span></label>
<label><input type="radio" name="${id}-type" value="INCOME"><span>수입</span></label>
</div>
</fieldset>
<fieldset class="choice-field">
<legend>카테고리</legend>
<div class="choice-buttons" id="${id}-categories"></div>
</fieldset>
<label class="field">
<span>내용</span>
<input type="text" id="${id}-title" maxlength="100" placeholder="예: 점심, 월급">
</label>
<fieldset class="choice-field">
<legend>결제수단</legend>
<div class="choice-buttons" id="${id}-payments"></div>
</fieldset>
<label class="field">
<span>날짜</span>
<input type="date" id="${id}-date">
</label>
<details class="optional-fields" id="${id}-memoBox">
<summary>메모 (선택)</summary>
<label class="field">
<span>메모</span>
<input type="text" id="${id}-memo" maxlength="500" placeholder="기억할 내용을 적어 주세요">
</label>
</details>
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
