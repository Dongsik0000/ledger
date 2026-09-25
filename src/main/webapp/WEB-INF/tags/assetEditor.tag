<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ attribute name="id" required="true"%>
<%-- 자산 추가·수정 창(화면에 하나). 값은 asset.js 가 채우고 showModal() 로 연다 --%>
<dialog class="editor-dialog" id="${id}" aria-labelledby="${id}-heading">
<header class="dialog-head">
<div>
<h2 id="${id}-heading">자산 추가</h2>
<p id="${id}-subtitle">필요한 내용을 입력해 주세요.</p>
</div>
<button type="button" class="icon-button" data-close aria-label="닫기">×</button>
</header>
<div class="dialog-body">
<fieldset class="form-stack">
<legend class="sr-only">자산 입력</legend>
<label class="field">
<span>자산 이름</span>
<input type="text" id="${id}-name" placeholder="예: 비상금 통장, 적금" maxlength="50">
</label>
<label class="field">
<span>현재 잔액 (원)</span>
<input type="text" id="${id}-amount" inputmode="numeric" autocomplete="off" placeholder="0" class="amount-input">
</label>
<p class="form-note">가계부에서 옮긴 돈은 지출로도 기록해 주세요.</p>
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
