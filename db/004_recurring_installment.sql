-- 고정 항목 할부: 총액을 개월 수로 나눠 첫 결제월부터 매달 기록하고, 마지막 회차 뒤 항목을 지운다.
-- 적용: psql -h 127.0.0.1 -U ledger_app -d ledger -f db/004_recurring_installment.sql

ALTER TABLE recurring_item
    ADD COLUMN installment_total  BIGINT,
    ADD COLUMN installment_months SMALLINT,
    ADD COLUMN installment_start  CHAR(7),
    ADD CONSTRAINT recurring_item_installment_check CHECK (
        (installment_total IS NULL AND installment_months IS NULL AND installment_start IS NULL)
        OR (installment_months BETWEEN 2 AND 60
            AND installment_total >= installment_months
            AND installment_start ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'
            AND type = 'EXPENSE')
    );

COMMENT ON COLUMN recurring_item.installment_total  IS '할부 총액(무이자). 일반 고정 항목은 NULL. amount 에는 2회차 이후 금액(총액 ÷ 개월 수, 나머지는 1회차)';
COMMENT ON COLUMN recurring_item.installment_months IS '할부 개월 수 2~60';
COMMENT ON COLUMN recurring_item.installment_start  IS '할부 첫 결제월 YYYY-MM. 이 달부터 개월 수만큼만 기록한다';
