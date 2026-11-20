-- 카테고리 예산: 지출 카테고리마다 한 주기(월급 주기)에 쓸 금액. 비어 있으면 예산 없음.
-- 사용액은 저장하지 않고 이번 주기 지출(환불 제외)로 매번 계산한다.
-- 적용: psql -h 127.0.0.1 -U ledger_app -d ledger -f db/011_category_budget.sql
ALTER TABLE category ADD COLUMN budget BIGINT CHECK (budget BETWEEN 1 AND 99999999999);
ALTER TABLE category ADD CONSTRAINT category_budget_expense CHECK (budget IS NULL OR type = 'EXPENSE');

COMMENT ON COLUMN category.budget IS '주기 예산(원, 지출 카테고리만). NULL 이면 예산 없음';
