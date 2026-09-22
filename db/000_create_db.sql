-- postgres 슈퍼유저로 1회 실행:  sudo -u postgres psql -f db/000_create_db.sql
-- 비밀번호는 실행 전에 바꾼다 (서버 /etc/ledger/app.env 의 LEDGER_DB_PASSWORD 와 동일해야 함)
CREATE ROLE ledger_app LOGIN PASSWORD 'ledger_app';
CREATE DATABASE ledger OWNER ledger_app ENCODING 'UTF8';
