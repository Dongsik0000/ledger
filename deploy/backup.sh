#!/usr/bin/env bash
# 매일 pg_dump 백업. 30일 보관.
#   sudo install -m 750 deploy/backup.sh /usr/local/bin/ledger-backup
#   sudo crontab -e  →  10 4 * * * /usr/local/bin/ledger-backup
set -euo pipefail

BACKUP_DIR=/var/backups/ledger
mkdir -p "$BACKUP_DIR"

sudo -u postgres pg_dump -Fc ledger > "$BACKUP_DIR/ledger-$(date +%F).dump"
find "$BACKUP_DIR" -name 'ledger-*.dump' -mtime +30 -delete
