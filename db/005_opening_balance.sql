-- 시작 잔액: 기준일이 시작될 때의 잔액. 잔액(이월·주기 잔액·누적)은 기준일 이전 거래를 빼고 이 금액부터 센다.
-- 적용: psql -h 127.0.0.1 -U ledger_app -d ledger -f db/005_opening_balance.sql

ALTER TABLE app_user
    ADD COLUMN opening_balance BIGINT NOT NULL DEFAULT 0
        CHECK (opening_balance BETWEEN -99999999999 AND 99999999999),
    ADD COLUMN opening_date DATE;

COMMENT ON COLUMN app_user.opening_balance IS '시작 잔액(원, 음수 가능). opening_date 가 없으면 쓰지 않는다';
COMMENT ON COLUMN app_user.opening_date    IS '시작 잔액 기준일. 이날 이전 거래는 잔액 계산에서 빼고 수입·지출 통계에는 남긴다. NULL 이면 모든 거래로 잔액을 센다';
