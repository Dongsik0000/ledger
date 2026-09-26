#!/usr/bin/env bash
# 매일 pg_dump 백업. 30일 보관.
#   sudo install -m 750 deploy/backup.sh /usr/local/bin/ledger-backup
#   매일 04:10 실행은 deploy/ledger-backup.service·timer (systemd, 서버에 crontab 없음)
set -euo pipefail
umask 077   # 덤프에 비밀번호 해시·거래 내역이 있으므로 root 만 읽게

BACKUP_DIR=/var/backups/ledger
mkdir -p "$BACKUP_DIR"

sudo -u postgres pg_dump -Fc ledger > "$BACKUP_DIR/ledger-$(date +%F).dump"
find "$BACKUP_DIR" -name 'ledger-*.dump' -mtime +30 -delete
