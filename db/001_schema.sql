-- ledger_app 으로 실행:  psql -U ledger_app -d ledger -f db/001_schema.sql
-- 모든 사용자 데이터 테이블은 user_id 로 격리한다. 조회 쿼리는 항상 AND user_id = #{userId} 를 건다.

CREATE TABLE app_user (
    id             BIGSERIAL PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    pay_day        SMALLINT     NOT NULL DEFAULT 1 CHECK (pay_day BETWEEN 1 AND 31),  -- 주기 시작일 (급여일). 1이면 달력 월
    pay_day_adjust BOOLEAN      NOT NULL DEFAULT FALSE,                              -- 주말/공휴일이면 직전 평일로
    created_at     TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE category (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES app_user(id),
    name       VARCHAR(50) NOT NULL,
    type       VARCHAR(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    group_name VARCHAR(50),                              -- 묶음 집계용 (예: 식비)
    sort_order INT         NOT NULL DEFAULT 0,
    active     BOOLEAN     NOT NULL DEFAULT TRUE,        -- 거래가 참조 중이면 삭제 대신 비활성
    UNIQUE (user_id, name, type)
);

CREATE TABLE payment_method (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES app_user(id),
    name       VARCHAR(50) NOT NULL,
    sort_order INT         NOT NULL DEFAULT 0,
    active     BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (user_id, name)
);

CREATE TABLE ledger_entry (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES app_user(id),
    entry_date        DATE         NOT NULL,
    type              VARCHAR(10)  NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    category_id       BIGINT       NOT NULL REFERENCES category(id),
    title             VARCHAR(100) NOT NULL,
    amount            BIGINT       NOT NULL CHECK (amount > 0),   -- 원 단위 양수. 수입/지출은 type 으로 구분
    payment_method_id BIGINT       REFERENCES payment_method(id),
    memo              VARCHAR(500),
    created_at        TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_ledger_entry_user_date ON ledger_entry (user_id, entry_date);

-- 고정 지출/수입 항목. 매일 00:05 스케줄러가 결제일이 된 항목을 ledger_entry 로 생성한다.
CREATE TABLE recurring_item (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES app_user(id),
    name              VARCHAR(100) NOT NULL,
    type              VARCHAR(10)  NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    amount            BIGINT       NOT NULL CHECK (amount > 0),
    category_id       BIGINT       NOT NULL REFERENCES category(id),
    payment_method_id BIGINT       REFERENCES payment_method(id),
    day_of_month      SMALLINT     NOT NULL CHECK (day_of_month BETWEEN 1 AND 31),  -- 짧은 달은 말일로
    adjust            VARCHAR(10)  NOT NULL DEFAULT 'NONE' CHECK (adjust IN ('NONE', 'PREV_BIZ', 'NEXT_BIZ')),
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    memo              VARCHAR(500)
);

-- 고정 항목의 월별 생성 기록. (recurring_id, period_ym) 유일 → 스케줄러가 여러 번 돌아도 중복 생성 안 됨.
-- 생성된 거래를 지워도 이 행은 남겨서 같은 달에 다시 만들지 않는다 (= 이번 달 건너뛰기).
CREATE TABLE recurring_run (
    recurring_id BIGINT    NOT NULL REFERENCES recurring_item(id) ON DELETE CASCADE,
    period_ym    CHAR(7)   NOT NULL,                       -- 'YYYY-MM'
    entry_id     BIGINT    REFERENCES ledger_entry(id) ON DELETE SET NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (recurring_id, period_ym)
);

-- 수동 입력 자산 잔액표 (적금, 주식 등). 총 잔액 = 가계부 누적 잔액 + 자산 합
CREATE TABLE asset (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES app_user(id),
    name       VARCHAR(50) NOT NULL,
    amount     BIGINT      NOT NULL DEFAULT 0,
    updated_at TIMESTAMP   NOT NULL DEFAULT now()
);

-- 공휴일 (전 사용자 공용). 공공데이터포털 특일정보 API 로 채우거나 설정 화면에서 수동 추가
CREATE TABLE holiday (
    holiday_date DATE        PRIMARY KEY,
    name         VARCHAR(50) NOT NULL
);
