# ledger — 개인 가계부

Spring Framework 6 (XML 설정) + MyBatis + PostgreSQL + JSP, Tomcat 10.1 / Java 17.
Oracle Cloud 서버의 nginx `/p3/` → Docker Tomcat `127.0.0.1:8081` 로 서비스한다.

- 다른 PC에서 이어서 하기: [docs/HANDOFF.md](docs/HANDOFF.md)
- 설계·기능 계획: [docs/plan.md](docs/plan.md)
- 서버 운영: [deploy/OPERATIONS.md](deploy/OPERATIONS.md)

## 로컬 실행

**1) DB** (로컬 PostgreSQL). `.sql` 은 UTF-8 이므로 Windows 에서는 먼저 `$env:PGCLIENTENCODING="UTF8"` 을 지정한다.

```bash
psql -U postgres -f db/000_create_db.sql
psql -U ledger_app -d ledger -f db/001_schema.sql
psql -U ledger_app -d ledger -f db/002_seed_holiday_2026.sql
psql -U ledger_app -d ledger -f db/003_comments.sql
```

Docker 컨테이너에 DB 를 만들 때는 `docker exec -i <컨테이너> psql -U <슈퍼유저> -d postgres < db/000_create_db.sql` 처럼 컨테이너 안에서 실행하면 비밀번호 없이 된다.

**2) 기동** 아래 "IntelliJ IDEA Ultimate 로 실행" → http://localhost:8081/p3/ (가입 코드 기본값: dev-signup, globals.properties 참고)

환경변수 `LEDGER_DB_URL`, `LEDGER_DB_USER`, `LEDGER_DB_PASSWORD`, `LEDGER_SIGNUP_CODE`, `LEDGER_HOLIDAY_API_KEY` 로 설정을 덮어쓸 수 있다.
예: Docker PostgreSQL 을 다른 포트로 띄웠다면 실행 구성의 `Startup/Connection` 탭 → Environment variables 에 `LEDGER_DB_URL=jdbc:postgresql://localhost:5434/ledger` 를 넣는다.

## IntelliJ IDEA Ultimate 로 실행

JSP·CSS·JS 를 저장하는 즉시 반영되고 중단점 디버깅도 된다.

**1) Tomcat 등록** (PC 당 1회)
`File → Settings → Build, Execution, Deployment → Application Servers` → `+` → Tomcat Server →
Tomcat Home 에 Tomcat 10.1 경로 입력. 없으면 https://tomcat.apache.org/download-10.cgi 에서 10.1.x 의 `64-bit Windows zip` 을 받아 압축을 푼 폴더를 지정한다.
Tomcat 9 이하(`javax`)는 Spring 6(`jakarta`)과 맞지 않아 쓸 수 없다.

**2) 실행 구성** `Run → Edit Configurations` → `+` → Tomcat Server → **Local**

| 탭 | 항목 | 값 |
|---|---|---|
| Server | URL (Open browser) | `http://localhost:8081/p3/` — 브라우저를 열 주소 |
| Server | On 'Update' action | `Update classes and resources` — 저장 시 즉시 반영 |
| Server | Tomcat Server Settings > **HTTP port** | **`8081`** — 실제 서버 포트. URL 과 별개 항목이다 |
| Deployment | Artifact | `ledger:war exploded` (`+` → Artifact 로 추가) |
| Deployment | **Application context** | **`/p3`** — 끝에 슬래시를 붙이면 배포가 실패한다 |

`ledger:war exploded` 가 목록에 없으면 Maven 창에서 Reload All Maven Projects 후 다시 연다.

**자주 나는 오류**

| 메시지 | 원인 |
|---|---|
| `Error during artifact deployment` | Application context 에 끝 슬래시(`/p3/`) |
| `Address localhost:1099 is already in use` | 이전 Tomcat 이 안 죽음. `netstat -ano -p tcp \| findstr :1099` 로 PID 확인 후 `taskkill /F /PID <PID>` |
| `Cannot open URL` | URL 만 바꾸고 HTTP port 를 안 바꿈 |

## 구조

```
src/main/java/ledger/
  cmmn/        공통 (Response, Constants, SessionUtil, ParamUtil, LikeUtil, CsvUtil, RequestUtil, LoginAttemptLimiter, CmmnExceptionHandler)
  interceptor/ ApiRequestInterceptor — POST 는 Ajax+JSON 만 / LoginPageInterceptor — /ledger/** 는 세션 userId 필수
  auth/        로그인·가입·로그아웃. AuthApiController 에 로직, AuthServiceImpl 은 DAO 연결만 (→ sqlmap/mappers/ledger/auth/auth.xml)
  settings/ entry/ dashboard/ recurring/ holiday/ summary/ asset/   각 기능, 같은 계층 구조(XxxApiController → XxxService → impl → XxxDAO → mapper)
  cycle/PayCycle                  주기·결제일 계산(순수, JUnit)
  recurring/RecurringGenerator    고정 항목 거래 생성(저장 직후·RecurringScheduler 매일 00:05·기동 시)
  holiday/HolidayApiClient, HolidayXmlParser   공공데이터포털 특일정보(XXE 차단)
  summary/SummaryTables           요약 표(카테고리 그룹 소계·결제수단)
src/main/resources/
  spring/context-datasource.xml   HikariCP, MyBatis, 트랜잭션, BCrypt
  sqlmap/mybatis-config.xml, sqlmap/mappers/ledger/<기능>/*.xml
  egovProps/globals.properties    ${ENV:기본값} 형태
src/main/webapp/
  WEB-INF/web.xml, WEB-INF/config/dispatcher-servlet.xml
  WEB-INF/tags/layout.tag         Tiles 대체 레이아웃. 화면 JSP 는 <t:layout title="">본문</t:layout>
  WEB-INF/layout/ledgerMenu.jsp   사이드 메뉴
  WEB-INF/jsp/ledger/<기능>/*.jsp
  resources/js/app/<기능>/*.js    App.xxx = (function(){ ... return {init} }()) 패턴
  resources/js/common/            common.js (App.post/get, App.h 안전한 DOM 생성, App.result 결과 코드 분기, 금액 입력), modal.js (_alert/_error/_confirm, 열린 dialog 위에 표시)
  resources/js/lib/chartjs/       Chart.js 4.4.6
db/                               번호 순서 SQL
deploy/                           nginx, app.env 예시, 백업, 운영 가이드
```

## 규칙

- 모든 사용자 데이터 쿼리는 `AND user_id = #{userId}` 를 건다. userId 는 `SessionUtil.getUserId(session)`.
- 금액은 원 단위 `bigint` 양수, 수입/지출은 `type` 컬럼으로 구분.
- 주기(급여일 기준)와 이월은 저장하지 않고 계산한다 — docs/plan.md "핵심 계산" 참고.
- 로직(검증·분기·계산·세션·`@Transactional`)은 `XxxApiController`, `ServiceImpl` 은 DAO 호출만. 컨트롤러에서 try/catch 금지(CmmnExceptionHandler 가 처리).
- 화면 데이터는 Ajax 로 그린다. 사용자 값은 `textContent`/`App.escape` 로만 HTML 에 넣는다. 결과 코드는 `App.CODE`.
