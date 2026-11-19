-- 환불·반납: 원래 거래와 같은 구분·카테고리로 적고 돈의 방향만 뒤집는다.
-- 지출 환불(EXPENSE + refund)은 잔액에 더하고 그 카테고리 지출에서 빼며, 수입 반납(INCOME + refund)은 잔액에서 빼고 수입에서 뺀다.
-- 적용: psql -h 127.0.0.1 -U ledger_app -d ledger -f db/010_entry_refund.sql
ALTER TABLE ledger_entry ADD COLUMN refund BOOLEAN NOT NULL DEFAULT FALSE;
-- 적금·예금 자산 이체는 지출 거래에만 반영하므로 환불과 함께 쓰지 않는다
ALTER TABLE ledger_entry ADD CONSTRAINT ledger_entry_refund_no_asset CHECK (NOT (refund AND transfer_asset_id IS NOT NULL));

COMMENT ON COLUMN ledger_entry.refund IS '환불·반납. TRUE 면 같은 구분의 반대 방향(지출 환불 = 들어온 돈, 수입 반납 = 나간 돈)';
