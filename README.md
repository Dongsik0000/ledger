# 🌿 Ledger — 월급날 기준으로 흐르는 가계부

> 작은 기록이 모여, 한 달의 흐름이 됩니다.

구글 시트로 쓰던 가계부를 웹으로 옮기며, 시트에서 늘 불편했던 세 가지를 해결했습니다.

| 시트에서의 불편 | Ledger 에서는 |
|---|---|
| 달이 바뀌면 남은 돈을 손으로 옮겨 적어야 했다 | **이월이 자동**입니다. 저장하지 않고 매번 계산하므로 과거 기록을 고쳐도 어긋나지 않습니다 |
| 월세·통신비·월급을 매달 다시 입력했다 | **고정 항목**을 한 번 등록하면 결제일에 알아서 기록됩니다 |
| 한 달의 기준이 1일이라 월급 흐름과 맞지 않았다 | **주기 시작일**(예: 25일)을 정하면 요약·잔액·일일 예산이 그 주기로 계산됩니다 |

PC 와 휴대폰 브라우저에서 같은 화면으로 씁니다.

---

## 주요 기능

**오늘의 가계부 (대시보드)**
- 이번 주기의 이월 · 수입 · 지출 · 잔액을 한눈에
- **오늘 쓸 수 있는 돈** = (주기 잔액 − 아직 나가지 않은 고정지출) ÷ 남은 날
- 빠른 거래 입력과 최근 기록

**거래 내역**
- 달 단위로 넘겨 보며 추가 · 수정 · 삭제
- 내용·메모 검색, 카테고리 · 결제수단 구분

**고정 항목**
- 수입과 지출 모두 등록, 결제일에 일반 거래로 자동 기록
- 결제일이 주말·공휴일이면 그대로 / 직전 영업일 / 다음 영업일 중 선택
- 서버가 잠시 꺼져 있었어도 다시 켜질 때 지난달 · 이번 달 결제일 중 빠진 것을 채우고, 같은 달에 두 번 기록되지 않습니다

**요약 / 통계**
- 월별 수입 · 지출 · 잔액 표와 막대 차트
- 카테고리 비중 도넛 차트, 그룹 소계가 있는 카테고리별 지출
- 결제수단별 월 지출

**자산**
- 통장 · 적금 · 주식 등의 잔액을 직접 적어 두는 잔액표
- 총 잔액 = 가계부 누적 잔액 + 자산 합

**설정**
- 수입 · 지출 카테고리(그룹 묶기)와 결제수단 관리 — 쓰고 있는 항목은 지우는 대신 숨김
- 주기 시작일(1~31일)과 주말 · 공휴일 보정
- 공휴일: 공공데이터포털 특일정보 API 로 연도별 가져오기 + 직접 추가
- 거래 내역 CSV 내보내기 (엑셀에서 바로 열리고, 수식 주입을 막습니다)

---

## 이렇게 만들었습니다

| 영역 | 사용 기술 |
|---|---|
| 서버 | Java 17, Spring Framework 6.2 (XML 설정), MyBatis 3.5, HikariCP |
| 화면 | JSP / JSTL 3.0, 순수 JavaScript (Ajax), Chart.js 4.4 |
| 데이터베이스 | PostgreSQL 16 |
| 실행 · 배포 | Tomcat 10.1, Docker Compose, nginx 리버스 프록시, Oracle Cloud (ARM64) |

**안전하게**
- 비밀번호는 BCrypt 로 저장하고, 로그인 5회 실패 시 5분 차단 · 로그인 후 세션 ID 교체
- 모든 데이터 조회에 사용자 조건을 걸어 다른 사람의 기록에 접근할 수 없습니다
- 데이터를 바꾸는 요청은 Ajax + JSON 만 받아 CSRF 를 막고, 화면에는 사용자 입력을 글자로만 넣어 XSS 를 막습니다
- 가입은 초대 코드가 있어야 가능합니다
- Tomcat 과 DB 는 서버 내부에만 열고, 외부 요청은 nginx 만 받습니다 (요청 크기 · 로그인 시도 속도 제한, 보안 헤더)

**정확하게**
- 주기 · 결제일 계산은 DB 와 분리된 순수 함수로 두고, 짧은 달 · 윤년 · 연속 공휴일 같은 경계 사례를 JUnit 으로 검증했습니다

---

## 직접 실행하기

<details>
<summary>로컬 실행</summary>

1. PostgreSQL 에 `db/` 의 SQL 을 번호 순서대로 적용합니다.
   ```bash
   psql -U postgres -f db/000_create_db.sql
   psql -U ledger_app -d ledger -f db/001_schema.sql
   psql -U ledger_app -d ledger -f db/002_seed_holiday_2026.sql
   psql -U ledger_app -d ledger -f db/003_comments.sql
   ```
2. `mvn package` 로 만든 `target/ledger.war` 를 Tomcat 10.1 에 컨텍스트 `/p3` 로 배포합니다.
3. http://localhost:8081/p3/ 에 접속합니다.

설정은 환경변수로 바꿉니다.

| 변수 | 용도 | 로컬 기본값 |
|---|---|---|
| `LEDGER_DB_URL` / `LEDGER_DB_USER` / `LEDGER_DB_PASSWORD` | DB 연결 | `localhost:5432/ledger`, `ledger_app` |
| `LEDGER_SIGNUP_CODE` | 가입 코드 | `dev-signup` |
| `LEDGER_HOLIDAY_API_KEY` | [공공데이터포털 특일정보](https://www.data.go.kr/data/15012690/openapi.do) 인증키 | 없음 (공휴일 직접 추가만 가능) |
| `LEDGER_LOG_LEVEL` | 앱 로그 수준 | `DEBUG` |

</details>

<details>
<summary>서버 배포 (Docker)</summary>

```bash
sudo mkdir -p /etc/ledger
sudo install -m 600 deploy/app.env.example /etc/ledger/app.env   # 값 입력
sudo docker compose up -d --build
```

- nginx 설정은 `deploy/nginx-ledger.conf`, `deploy/nginx-ledger-limit.conf` 파일 머리말을 따릅니다.
- 매일 백업은 `deploy/backup.sh` 를 크론에 등록합니다.
- 운영 이미지는 로그 `INFO`, 가입 코드가 없으면 가입이 막힌 상태로 뜹니다.

</details>
