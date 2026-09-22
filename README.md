# ledger — 개인 가계부

Spring Framework 6 (XML 설정) + MyBatis + PostgreSQL + JSP, Tomcat 10.1 / Java 17.
Oracle Cloud 서버의 nginx `/p3/` → Docker Tomcat `127.0.0.1:8081` 로 서비스한다.

- 설계·기능 계획: [docs/plan.md](docs/plan.md)
- 서버 운영: [deploy/OPERATIONS.md](deploy/OPERATIONS.md)

## 로컬 실행

```bash
# 1. DB (로컬 PostgreSQL)
psql -U postgres -f db/000_create_db.sql
psql -U ledger_app -d ledger -f db/001_schema.sql
psql -U ledger_app -d ledger -f db/002_seed_holiday_2026.sql

# 2. 기동 (Tomcat 10.1 을 자동으로 받아서 띄운다)
mvn cargo:run
# → http://localhost:8081/p3/  (가입 코드 기본값: dev-signup, globals.properties 참고)
```

환경변수 `LEDGER_DB_URL`, `LEDGER_DB_USER`, `LEDGER_DB_PASSWORD`, `LEDGER_SIGNUP_CODE`, `LEDGER_HOLIDAY_API_KEY` 로 설정을 덮어쓸 수 있다.

## 구조

```
src/main/java/ledger/
  cmmn/        공통 (Response, Constants, SessionUtil, CmmnController: /login /signup /logout)
  interceptor/ LoginPageInterceptor — /ledger/** 는 세션 userId 필수
  auth/        로그인·가입 (controller → service/impl → dao → sqlmap/mappers/ledger/auth/auth.xml)
  dashboard/ entry/ recurring/ summary/ asset/ settings/   각 기능, 같은 계층 구조
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
  resources/js/common/            common.js (App.post/get, _loading), modal.js (_alert/_error/_confirm)
  resources/js/lib/chartjs/       Chart.js 4.4.6
db/                               번호 순서 SQL
deploy/                           nginx, app.env 예시, 백업, 운영 가이드
```

## 규칙

- 모든 사용자 데이터 쿼리는 `AND user_id = #{userId}` 를 건다. userId 는 `SessionUtil.getUserId(session)`.
- 금액은 원 단위 `bigint` 양수, 수입/지출은 `type` 컬럼으로 구분.
- 주기(급여일 기준)와 이월은 저장하지 않고 계산한다 — docs/plan.md "핵심 계산" 참고.
