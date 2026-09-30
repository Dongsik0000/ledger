-- 통장은 결제수단과 떼어 거래·고정 항목이 직접 고른다(결제수단은 체크카드·계좌이체 같은 결제 방식으로 남는다).
-- 기본 통장은 잔액을 적지 않고 "가계부 잔액 − 다른 통장 잔액"으로 계산해, 통장 합계가 항상 가계부 잔액과 같다.
-- 통장을 고르지 않은 거래(account_id NULL)는 기본 통장에 들어간다.
-- 이 파일의 이전 초안(is_default·신용카드 제약만 추가)을 적용한 DB에서도 그대로 실행되도록 IF [NOT] EXISTS 를 쓴다.
-- 적용: psql -h 127.0.0.1 -U ledger_app -d ledger -f db/009_default_account.sql

ALTER TABLE bank_account
    DROP CONSTRAINT IF EXISTS bank_account_default_bank,
    DROP COLUMN IF EXISTS payment_method_id,
    DROP COLUMN IF EXISTS kind,
    ADD COLUMN IF NOT EXISTS is_default BOOLEAN NOT NULL DEFAULT FALSE;
CREATE UNIQUE INDEX IF NOT EXISTS bank_account_one_default ON bank_account (user_id) WHERE is_default;

ALTER TABLE ledger_entry ADD COLUMN IF NOT EXISTS account_id BIGINT REFERENCES bank_account(id);
CREATE INDEX IF NOT EXISTS idx_ledger_entry_account ON ledger_entry (account_id) WHERE account_id IS NOT NULL;
ALTER TABLE recurring_item ADD COLUMN IF NOT EXISTS account_id BIGINT REFERENCES bank_account(id);

COMMENT ON COLUMN bank_account.is_default IS '기본 통장(사용자당 하나). 기준 잔액·기준일은 쓰지 않는다';
COMMENT ON COLUMN ledger_entry.account_id IS '돈이 들어오고 나간 통장. NULL 이면 기본 통장';
COMMENT ON COLUMN recurring_item.account_id IS '자동 기록할 거래의 통장. NULL 이면 기본 통장';
