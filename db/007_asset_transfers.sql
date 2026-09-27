-- 지출 거래와 현금성 자산의 연결. 삭제된 자산은 연결만 해제하고 거래 기록은 보존한다.
ALTER TABLE ledger_entry ADD COLUMN transfer_asset_id BIGINT REFERENCES asset(id) ON DELETE SET NULL;
ALTER TABLE ledger_entry ADD CONSTRAINT ledger_entry_transfer_expense_only
    CHECK (transfer_asset_id IS NULL OR type = 'EXPENSE');
CREATE INDEX idx_ledger_entry_transfer_asset ON ledger_entry (transfer_asset_id)
    WHERE transfer_asset_id IS NOT NULL;

ALTER TABLE recurring_item ADD COLUMN transfer_asset_id BIGINT REFERENCES asset(id) ON DELETE SET NULL;
ALTER TABLE recurring_item ADD CONSTRAINT recurring_transfer_expense_only
    CHECK (transfer_asset_id IS NULL OR type = 'EXPENSE');
