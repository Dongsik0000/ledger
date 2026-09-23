<%@ tag language="java" pageEncoding="UTF-8" body-content="empty"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ attribute name="id" required="true"%>
<%@ attribute name="heading" required="true"%>
<%@ attribute name="subtitle" required="false"%>
<%@ attribute name="name" required="false"%>
<%@ attribute name="amount" required="false"%>
<%@ attribute name="editing" required="false"%>
<%@ attribute name="kind" required="false"%>
<%@ attribute name="category" required="false"%>
<%@ attribute name="payment" required="false"%>
<%@ attribute name="date" required="false"%>
<%@ attribute name="memo" required="false"%>
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
<span>금액 (원)</span>
<input type="text" name="${id}-amount" value="<c:out value="${amount}"/>" inputmode="numeric" pattern="[0-9,]+" autocomplete="off" placeholder="0" class="amount-input">
</label>
<fieldset class="choice-field">
<legend>구분</legend>
<div class="choice-buttons">
<label>
<input type="radio" name="${id}-kind" value="EXPENSE" ${kind eq 'EXPENSE' ? 'checked' : ''}>
<span>지출</span>
</label>
<label>
<input type="radio" name="${id}-kind" value="INCOME" ${kind eq 'INCOME' ? 'checked' : ''}>
<span>수입</span>
</label>
</div>
</fieldset>
<fieldset class="choice-field">
<legend>카테고리</legend>
<div class="choice-buttons">
<label>
<input type="radio" name="${id}-category" value="식비" ${category eq '식비' ? 'checked' : ''}>
<span>식비</span>
</label>
<label>
<input type="radio" name="${id}-category" value="배달" ${category eq '배달' ? 'checked' : ''}>
<span>배달</span>
</label>
<label>
<input type="radio" name="${id}-category" value="카페/간식" ${category eq '카페/간식' ? 'checked' : ''}>
<span>카페/간식</span>
</label>
<label>
<input type="radio" name="${id}-category" value="장보기" ${category eq '장보기' ? 'checked' : ''}>
<span>장보기</span>
</label>
<label>
<input type="radio" name="${id}-category" value="교통" ${category eq '교통' ? 'checked' : ''}>
<span>교통</span>
</label>
<label>
<input type="radio" name="${id}-category" value="고정지출" ${category eq '고정지출' ? 'checked' : ''}>
<span>고정지출</span>
</label>
<label>
<input type="radio" name="${id}-category" value="저축" ${category eq '저축' ? 'checked' : ''}>
<span>저축</span>
</label>
<label>
<input type="radio" name="${id}-category" value="기타" ${category eq '기타' ? 'checked' : ''}>
<span>기타</span>
</label>
<label>
<input type="radio" name="${id}-category" value="급여" ${category eq '급여' ? 'checked' : ''}>
<span>급여</span>
</label>
</div>
</fieldset>
<label class="field">
<span>내용</span>
<input type="text" name="${id}-name" value="<c:out value="${name}"/>" maxlength="100" placeholder="예: 점심, 월급">
</label>
<fieldset class="choice-field">
<legend>결제수단</legend>
<div class="choice-buttons">
<label>
<input type="radio" name="${id}-payment" value="신용카드" ${payment eq '신용카드' ? 'checked' : ''}>
<span>신용카드</span>
</label>
<label>
<input type="radio" name="${id}-payment" value="체크카드" ${payment eq '체크카드' ? 'checked' : ''}>
<span>체크카드</span>
</label>
<label>
<input type="radio" name="${id}-payment" value="계좌이체" ${payment eq '계좌이체' ? 'checked' : ''}>
<span>계좌이체</span>
</label>
<label>
<input type="radio" name="${id}-payment" value="현금" ${payment eq '현금' ? 'checked' : ''}>
<span>현금</span>
</label>
</div>
</fieldset>
<label class="field">
<span>날짜</span>
<input type="date" name="${id}-date" value="<c:out value="${date}"/>" >
</label>
<details class="optional-fields" ${not empty memo ? 'open' : ''}>
<summary>메모 (선택)</summary>
<label class="field">
<span>메모</span>
<input type="text" name="${id}-memo" value="<c:out value="${memo}"/>" maxlength="500" placeholder="기억할 내용을 적어 주세요">
</label>
</details>
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