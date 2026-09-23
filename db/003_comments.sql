-- 테이블/컬럼 주석. DBeaver 에서 테이블을 열면 Description 열에 표시된다.
-- 적용: docker exec -i my-postgres psql -U ledger_app -d ledger < db/003_comments.sql

COMMENT ON TABLE app_user IS '사용자 계정. 모든 데이터는 이 id 로 격리된다';
COMMENT ON COLUMN app_user.id             IS '사용자 PK. 다른 테이블의 user_id 가 참조';
COMMENT ON COLUMN app_user.username       IS '로그인 아이디 (중복 불가)';
COMMENT ON COLUMN app_user.password_hash  IS 'BCrypt 해시 (평문 저장 안 함)';
COMMENT ON COLUMN app_user.pay_day        IS '주기 시작일 1~31. 급여일 기준으로 요약/잔액/일일예산을 계산한다. 1이면 달력 월과 동일';
COMMENT ON COLUMN app_user.pay_day_adjust IS 'true 면 주기 시작일이 주말/공휴일일 때 직전 평일로 당긴다';
COMMENT ON COLUMN app_user.created_at     IS '가입 일시';

COMMENT ON TABLE category IS '수입/지출 분류. 이름을 바꿔도 거래는 id 로 참조하므로 과거 데이터가 어긋나지 않는다';
COMMENT ON COLUMN category.id         IS '카테고리 PK';
COMMENT ON COLUMN category.user_id    IS '소유 사용자';
COMMENT ON COLUMN category.name       IS '표시 이름 (예: 식비, 배달, 급여)';
COMMENT ON COLUMN category.type       IS 'INCOME=수입 / EXPENSE=지출';
COMMENT ON COLUMN category.group_name IS '묶음 집계용 그룹명. 예: 식비/배달/카페에 모두 "식비" 를 넣으면 "총 식비" 합계가 나온다. NULL 이면 묶지 않음';
COMMENT ON COLUMN category.sort_order IS '화면 표시 순서 (작을수록 먼저)';
COMMENT ON COLUMN category.active     IS 'false 면 목록에서 숨김. 거래가 참조 중인 카테고리는 삭제 대신 이 값을 false 로';

COMMENT ON TABLE payment_method IS '결제수단 (신용카드, 체크카드, 계좌이체, 현금 등)';
COMMENT ON COLUMN payment_method.id         IS '결제수단 PK';
COMMENT ON COLUMN payment_method.user_id    IS '소유 사용자';
COMMENT ON COLUMN payment_method.name       IS '표시 이름';
COMMENT ON COLUMN payment_method.sort_order IS '화면 표시 순서';
COMMENT ON COLUMN payment_method.active     IS 'false 면 목록에서 숨김 (삭제 대신 사용)';

COMMENT ON TABLE ledger_entry IS '거래 내역. 가계부의 실제 기록';
COMMENT ON COLUMN ledger_entry.id                IS '거래 PK';
COMMENT ON COLUMN ledger_entry.user_id           IS '소유 사용자. 모든 조회 쿼리에 이 조건을 건다';
COMMENT ON COLUMN ledger_entry.entry_date        IS '거래 날짜 (달력 화면과 주기 집계의 기준)';
COMMENT ON COLUMN ledger_entry.type              IS 'INCOME=수입 / EXPENSE=지출. 금액은 항상 양수이고 이 값으로 부호를 구분한다';
COMMENT ON COLUMN ledger_entry.category_id       IS '카테고리';
COMMENT ON COLUMN ledger_entry.title             IS '내용 (예: 점심, 지하철)';
COMMENT ON COLUMN ledger_entry.amount            IS '금액. 원 단위 정수, 항상 양수';
COMMENT ON COLUMN ledger_entry.payment_method_id IS '결제수단. 수입이면 비어 있을 수 있음';
COMMENT ON COLUMN ledger_entry.memo              IS '메모';
COMMENT ON COLUMN ledger_entry.created_at        IS '입력 일시';

COMMENT ON TABLE recurring_item IS '고정 지출/수입 항목 (월세, 구독료, 월급 등). 결제일이 되면 스케줄러가 거래를 자동 생성한다';
COMMENT ON COLUMN recurring_item.id                IS '고정 항목 PK';
COMMENT ON COLUMN recurring_item.user_id           IS '소유 사용자';
COMMENT ON COLUMN recurring_item.name              IS '항목 이름 (예: 넷플릭스, 월세)';
COMMENT ON COLUMN recurring_item.type              IS 'INCOME=수입 / EXPENSE=지출';
COMMENT ON COLUMN recurring_item.amount            IS '기본 금액. 공과금처럼 매달 다르면 생성된 거래를 수정한다';
COMMENT ON COLUMN recurring_item.category_id       IS '생성될 거래의 카테고리';
COMMENT ON COLUMN recurring_item.payment_method_id IS '생성될 거래의 결제수단';
COMMENT ON COLUMN recurring_item.day_of_month      IS '결제일 1~31. 그 달에 없는 날짜면 말일로 당긴다';
COMMENT ON COLUMN recurring_item.adjust            IS '결제일이 주말/공휴일일 때: NONE=그대로, PREV_BIZ=직전 평일, NEXT_BIZ=다음 평일';
COMMENT ON COLUMN recurring_item.active            IS 'false 면 자동 생성하지 않음';
COMMENT ON COLUMN recurring_item.memo              IS '메모';

COMMENT ON TABLE recurring_run IS '고정 항목의 월별 생성 기록. 같은 달에 두 번 만들어지는 것을 막는다. 생성된 거래를 지워도 이 행은 남으므로 그 달은 다시 생성되지 않는다(= 건너뛰기)';
COMMENT ON COLUMN recurring_run.recurring_id IS '고정 항목';
COMMENT ON COLUMN recurring_run.period_ym    IS '대상 월 (YYYY-MM)';
COMMENT ON COLUMN recurring_run.entry_id     IS '생성된 거래. 거래를 삭제하면 NULL 이 된다';
COMMENT ON COLUMN recurring_run.created_at   IS '생성 일시';

COMMENT ON TABLE asset IS '자산 잔액표 (적금, 주식 등). 수동으로 갱신한다. 총 잔액 = 가계부 누적 잔액 + 이 합계';
COMMENT ON COLUMN asset.id         IS '자산 PK';
COMMENT ON COLUMN asset.user_id    IS '소유 사용자';
COMMENT ON COLUMN asset.name       IS '자산 이름 (예: 청약저축, 연금)';
COMMENT ON COLUMN asset.amount     IS '현재 금액. 적금/주식으로 옮긴 돈은 "저축" 카테고리 지출로도 기록해야 이중 계산되지 않는다';
COMMENT ON COLUMN asset.updated_at IS '마지막 갱신 일시';

COMMENT ON TABLE holiday IS '공휴일 (전 사용자 공용). 주기 시작일/결제일의 평일 보정에 쓴다. 공공데이터포털 특일정보 API 로 채우거나 직접 추가';
COMMENT ON COLUMN holiday.holiday_date IS '공휴일 날짜';
COMMENT ON COLUMN holiday.name         IS '공휴일 이름';
