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
<label class="checkbox-field">
<input type="checkbox" id="${id}-stock">
<span>미국 주식 시세로 평가</span>
</label>
<label class="field" data-cash>
<span>현재 잔액 (원)</span>
<input type="text" id="${id}-amount" inputmode="numeric" autocomplete="off" placeholder="0" class="amount-input">
</label>
<div class="field" data-stock hidden>
<label for="${id}-stock-search">회사·ETF 영문명 또는 종목 코드</label>
<div class="stock-search-control">
<input type="search" id="${id}-stock-search" maxlength="60" autocomplete="off" placeholder="예: Tesla, SPYM">
<button type="button" class="button" id="${id}-stock-search-button">종목 찾기</button>
</div>
<p class="form-note" id="${id}-stock-search-status" role="status" aria-live="polite">검색 결과에서 미국 상장 종목을 선택해 주세요.</p>
<div class="stock-search-results" id="${id}-stock-search-results"></div>
</div>
<div class="field-row" data-stock hidden>
<label class="field">
<span>선택한 종목 코드 (직접 입력 가능)</span>
<input type="text" id="${id}-symbol" maxlength="12" autocapitalize="characters" placeholder="TSLA 또는 SPYM">
</label>
<label class="field">
<span>보유 수량 (주)</span>
<input type="number" id="${id}-quantity" min="0.000001" step="0.000001" inputmode="decimal" placeholder="2">
</label>
</div>
<p class="form-note" data-stock hidden>미국 주식의 최근 제공 시세와 USD/KRW 환율로 평가해요. 시세 기준일을 자산 목록에 표시해요.</p>
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
