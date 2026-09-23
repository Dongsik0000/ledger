<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="heading" required="true"%>
<%@ attribute name="subtitle" required="false"%>
<%@ attribute name="name" required="false"%>
<%@ attribute name="amount" required="false"%>
<%@ attribute name="editing" required="false"%>
<dialog class="editor-dialog" id="${id}" aria-labelledby="${id}-heading" aria-describedby="${id}-note">
<header class="dialog-head">
<div>
<h2 id="${id}-heading">
<c:out value="${heading}"/>
</h2>
<p>
<c:out value="${subtitle}"/>
</p>
</div>
<button type="button" class="icon-button" commandfor="${id}" command="close" aria-label="닫기">×</button>
</header>
<div class="dialog-body">
<fieldset class="form-stack">
<legend class="sr-only">
<c:out value="${heading}"/>
</legend>
<label class="field">
<span>자산 이름</span>
<input type="text" name="${id}-name" value="<c:out value="${name}"/>" placeholder="예: 비상금 통장, 적금" maxlength="100">
</label>
<label class="field">
<span>현재 잔액 (원)</span>
<input type="number" name="${id}-amount" value="<c:out value="${amount}"/>" step="1" inputmode="numeric" placeholder="0">
</label>
<p class="form-note">가계부에서 옮긴 돈은 지출로도 기록해 주세요.</p>
</fieldset>
</div>
<footer class="dialog-footer">
<p class="form-note" id="${id}-note">디자인 미리보기 · 입력한 내용은 저장되지 않아요.</p>
<div class="form-actions">
<c:if test="${editing}">
<button type="button" class="button danger" commandfor="${id}-delete" command="show-modal">삭제…</button>
</c:if>
<button type="button" class="button" commandfor="${id}" command="close">닫기</button>
<button type="button" class="button primary" disabled title="저장 기능은 연결되지 않은 화면입니다">저장</button>
</div>
</footer>
</dialog>
<c:if test="${editing}">
<dialog class="editor-dialog confirm-dialog" id="${id}-delete" aria-labelledby="${id}-delete-heading">
<header class="dialog-head">
<h2 id="${id}-delete-heading">삭제 전 확인</h2>
<button type="button" class="icon-button" commandfor="${id}-delete" command="close" aria-label="확인창 닫기">×</button>
</header>
<div class="dialog-body">
<p>
<strong>
<c:out value="${name}"/>
</strong> 항목을 삭제할까요?</p>
<p class="form-note">이 화면에서는 실제로 삭제되지 않아요.</p>
</div>
<footer class="dialog-footer">
<div class="form-actions">
<button type="button" class="button" commandfor="${id}-delete" command="close">돌아가기</button>
<button type="button" class="button danger" disabled>삭제</button>
</div>
</footer>
</dialog>
</c:if>