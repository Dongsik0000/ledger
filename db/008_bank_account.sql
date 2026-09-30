-- 통장(체크카드 연결)별 잔액과 통장 간 이체. 잔액은 저장하지 않고 기준 잔액 + 기준일 이후 기록으로 매번 계산한다.
-- 이체는 수입·지출이 아니므로 ledger_entry 에 넣지 않는다(가계부 잔액·통계에 영향 없음).
-- 적용: psql -h 127.0.0.1 -U ledger_app -d ledger -f db/008_bank_account.sql

CREATE TABLE bank_account (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT      NOT NULL REFERENCES app_user(id),
    name              VARCHAR(50) NOT NULL,
    kind              VARCHAR(10) NOT NULL DEFAULT 'BANK' CHECK (kind IN ('BANK', 'CREDIT')),
    payment_method_id BIGINT      UNIQUE REFERENCES payment_method(id),
    base_balance      BIGINT      NOT NULL CHECK (base_balance BETWEEN -99999999999 AND 99999999999),
    base_date         DATE        NOT NULL,
    base_at           TIMESTAMP   NOT NULL DEFAULT now(),
    created_at        TIMESTAMP   NOT NULL DEFAULT now(),
    UNIQUE (user_id, name)
);

COMMENT ON COLUMN bank_account.kind              IS 'BANK 통장, CREDIT 신용카드(잔액이 음수면 결제할 금액)';
COMMENT ON COLUMN bank_account.payment_method_id IS '연결 결제수단(체크카드·신용카드). 이 결제수단의 수입은 더하고 지출은 뺀다';
COMMENT ON COLUMN bank_account.base_balance      IS '기준 잔액(원, 음수 가능). 기준일 거래 중 base_at 전에 기록한 것까지 반영된 금액';
COMMENT ON COLUMN bank_account.base_date         IS '기준일. 다음 날부터 오늘까지의 거래·이체와, 기준일 거래 중 base_at 뒤에 기록한 것을 더한다';
COMMENT ON COLUMN bank_account.base_at           IS '기준 잔액을 적은 시각. 기준 잔액·기준일을 바꿀 때만 갱신한다(은행 앱 잔액에 이미 들어간 오늘 거래를 두 번 빼지 않게)';

CREATE TABLE account_transfer (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id),
    transfer_date   DATE         NOT NULL,
    from_account_id BIGINT       NOT NULL REFERENCES bank_account(id),
    to_account_id   BIGINT       NOT NULL REFERENCES bank_account(id),
    amount          BIGINT       NOT NULL CHECK (amount > 0),
    memo            VARCHAR(500),
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    CHECK (from_account_id <> to_account_id)
);
CREATE INDEX idx_account_transfer_user_date ON account_transfer (user_id, transfer_date);

-- 정기 이체. 매일 00:05 스케줄러가 이체일이 된 항목을 account_transfer 로 기록한다(고정 항목과 같은 결제일 계산)
CREATE TABLE recurring_transfer (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES app_user(id),
    from_account_id BIGINT       NOT NULL REFERENCES bank_account(id) ON DELETE CASCADE,
    to_account_id   BIGINT       NOT NULL REFERENCES bank_account(id) ON DELETE CASCADE,
    amount          BIGINT       NOT NULL CHECK (amount > 0),
    day_of_month    SMALLINT     NOT NULL CHECK (day_of_month BETWEEN 1 AND 31),
    adjust          VARCHAR(10)  NOT NULL DEFAULT 'NONE' CHECK (adjust IN ('NONE', 'PREV_BIZ', 'NEXT_BIZ')),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    memo            VARCHAR(500),
    CHECK (from_account_id <> to_account_id)
);

-- 정기 이체의 월별 처리 기록. recurring_run 과 같이 이체를 지워도 행을 남겨 같은 달에 다시 만들지 않는다
CREATE TABLE recurring_transfer_run (
    recurring_transfer_id BIGINT    NOT NULL REFERENCES recurring_transfer(id) ON DELETE CASCADE,
    period_ym             CHAR(7)   NOT NULL,
    transfer_id           BIGINT    REFERENCES account_transfer(id) ON DELETE SET NULL,
    created_at            TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (recurring_transfer_id, period_ym)
);
