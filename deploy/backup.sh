#!/usr/bin/env bash
# 매일 pg_dump 백업. 30일 보관.
#   sudo install -m 750 deploy/backup.sh /usr/local/bin/ledger-backup
#   매일 04:10 실행은 deploy/ledger-backup.service·timer (systemd, 서버에 crontab 없음)
set -euo pipefail
umask 077   # 덤프에 비밀번호 해시·거래 내역이 있으므로 root 만 읽게

BACKUP_DIR=/var/backups/ledger
mkdir -p "$BACKUP_DIR"

# 시각까지 넣어 같은 날 다시 실행해도 기존 백업을 덮어쓰지 않는다.
# 임시 파일에 다 쓴 뒤 성공했을 때만 이름을 바꾸므로 .dump 는 끝까지 성공한 백업만 남는다
FILE="$BACKUP_DIR/ledger-$(date +%F-%H%M%S).dump"
trap 'rm -f "$FILE.tmp"' EXIT

sudo -u postgres pg_dump -Fc ledger > "$FILE.tmp"
mv "$FILE.tmp" "$FILE"
find "$BACKUP_DIR" -name 'ledger-*.dump' -mtime +30 -delete
