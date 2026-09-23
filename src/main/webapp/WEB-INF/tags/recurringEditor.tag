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
<%@ attribute name="day" required="false"%>
<%@ attribute name="adjust" required="false"%>
<%@ attribute name="memo" required="false"%>
<%@ attribute name="active" required="false"%>
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
<span>항목명</span>
<input type="text" name="${id}-name" value="<c:out value="${name}"/>" placeholder="예: 월세, 구독료, 월급" maxlength="100">
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
<div class="field-row">
<label class="field">
<span>금액 (원)</span>
<input type="number" name="${id}-amount" value="<c:out value="${amount}"/>" min="1" step="1" inputmode="numeric">
</label>
<label class="field">
<span>매월 결제일</span>
<input type="number" name="${id}-day" value="<c:out value="${day}"/>" min="1" max="31" inputmode="numeric">
</label>
</div>
<div class="field-row">
<label class="field">
<span>카테고리</span>
<select name="${id}-category">
<option value="식비" ${category eq '식비' ? 'selected' : ''}>식비</option>
<option value="배달" ${category eq '배달' ? 'selected' : ''}>배달</option>
<option value="카페/간식" ${category eq '카페/간식' ? 'selected' : ''}>카페/간식</option>
<option value="장보기" ${category eq '장보기' ? 'selected' : ''}>장보기</option>
<option value="교통" ${category eq '교통' ? 'selected' : ''}>교통</option>
<option value="고정지출" ${category eq '고정지출' ? 'selected' : ''}>고정지출</option>
<option value="저축" ${category eq '저축' ? 'selected' : ''}>저축</option>
<option value="기타" ${category eq '기타' ? 'selected' : ''}>기타</option>
<option value="급여" ${category eq '급여' ? 'selected' : ''}>급여</option>
</select>
</label>
<label class="field">
<span>결제수단</span>
<select name="${id}-payment">
<option value="신용카드" ${payment eq '신용카드' ? 'selected' : ''}>신용카드</option>
<option value="체크카드" ${payment eq '체크카드' ? 'selected' : ''}>체크카드</option>
<option value="계좌이체" ${payment eq '계좌이체' ? 'selected' : ''}>계좌이체</option>
<option value="현금" ${payment eq '현금' ? 'selected' : ''}>현금</option>
</select>
</label>
</div>
<fieldset class="choice-field">
<legend>주말·공휴일인 경우</legend>
<div class="choice-buttons">
<label>
<input type="radio" name="${id}-adjust" value="NONE" ${adjust eq 'NONE' ? 'checked' : ''}>
<span>그대로</span>
</label>
<label>
<input type="radio" name="${id}-adjust" value="PREV_BIZ" ${adjust eq 'PREV_BIZ' ? 'checked' : ''}>
<span>직전 평일</span>
</label>
<label>
<input type="radio" name="${id}-adjust" value="NEXT_BIZ" ${adjust eq 'NEXT_BIZ' ? 'checked' : ''}>
<span>다음 평일</span>
</label>
</div>
</fieldset>
<p class="form-note">해당 날짜가 없는 달은 그 달의 마지막 날을 기준으로 해요.</p>
<label class="field">
<span>메모 (선택)</span>
<input type="text" name="${id}-memo" value="<c:out value="${memo}"/>" maxlength="500">
</label>
<label class="checkbox-field">
<input type="checkbox" name="${id}-active" ${active eq 'true' ? 'checked' : ''}>
<span>자동 기록 활성화</span>
</label>
<p class="form-note">끄면 이후 자동 기록에서 제외돼요. 이전 거래는 남아 있어요.</p>
</fieldset>
</div>
<footer class="dialog-footer">
<p class="form-note" id="${id}-note">디자인 미리보기 · 입력한 내용은 저장되지 않아요.</p>
<div class="form-actions">
<button type="button" class="button" commandfor="${id}" command="close">닫기</button>
<button type="button" class="button primary" disabled title="저장 기능은 연결되지 않은 화면입니다">저장</button>
</div>
</footer>
</dialog>
