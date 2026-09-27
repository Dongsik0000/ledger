-- 외화 고정지출과 미국 주식 보유 자산. 운영 DB에 배포 전 한 번 적용한다.
ALTER TABLE recurring_item ADD COLUMN usd_amount NUMERIC(12, 2)
    CHECK (usd_amount > 0 AND usd_amount <= 9999999999.99);
ALTER TABLE recurring_item ADD CONSTRAINT recurring_usd_no_installment
    CHECK (usd_amount IS NULL OR (type = 'EXPENSE' AND installment_total IS NULL));

ALTER TABLE recurring_run ADD COLUMN fx_rate NUMERIC(20, 10),
    ADD COLUMN fx_as_of VARCHAR(30);

ALTER TABLE asset ADD COLUMN stock_symbol VARCHAR(12),
    ADD COLUMN stock_quantity NUMERIC(18, 6),
    ADD COLUMN stock_price_usd NUMERIC(20, 10),
    ADD COLUMN stock_usd_krw NUMERIC(20, 10),
    ADD COLUMN stock_quote_date DATE,
    ADD COLUMN stock_valued_at TIMESTAMP;
ALTER TABLE asset ADD CONSTRAINT asset_stock_fields CHECK (
    (stock_symbol IS NULL AND stock_quantity IS NULL AND stock_price_usd IS NULL
        AND stock_usd_krw IS NULL AND stock_quote_date IS NULL AND stock_valued_at IS NULL)
    OR (stock_symbol ~ '^[A-Z.]{1,12}$' AND stock_quantity > 0)
);
