#!/usr/bin/env bash
# GitHub Actions 가 SSH 로 실행하는 배포 스크립트 (서버: /srv/devgear/ledger)
#   bash /srv/devgear/ledger/deploy/deploy.sh
#
# 전체를 main 함수로 감싼다 — git pull 이 이 파일 자체를 바꿔도 bash 는 이미 읽은 함수를 끝까지 실행한다.
set -euo pipefail

main() {
    # 같은 서버의 다른 앱(ledger·fashion-archive·workout) 배포와 빌드가 겹치지 않게 한 번에 하나씩 실행한다
    exec 9>/tmp/devgear-deploy.lock
    flock -w 900 9 || { echo "다른 앱 배포가 15분 안에 끝나지 않아 중단합니다" >&2; return 1; }

    cd /srv/devgear/ledger
    git pull --ff-only

    # 지금 이미지를 ledger:prev 로 남긴다. 새 이미지가 healthy 가 되지 않으면 이것으로 되돌리고, 아래 정리 단계에서도 지워지지 않는다
    docker image inspect ledger:local > /dev/null 2>&1 && docker tag ledger:local ledger:prev

    # 이미지 HEALTHCHECK(/p3/health: DB 연결까지 확인)가 healthy 가 될 때까지 최대 4분 기다린다.
    # 실패하면 직전 이미지로 되돌리고 실패로 끝내 Actions 에 표시되게 한다(정리 단계도 건너뛴다)
    if ! docker compose up -d --build --wait --wait-timeout 240; then
        echo "배포 후 앱이 healthy 가 되지 않습니다: docker compose logs 로 확인하세요" >&2
        if docker image inspect ledger:prev > /dev/null 2>&1; then
            docker tag ledger:prev ledger:local
            docker compose up -d --no-build \
                && echo "직전 이미지로 되돌렸습니다. 서버 코드는 $(git log --oneline -1) 그대로입니다" >&2
        fi
        return 1
    fi

    # 빌드할수록 쌓이는 이름 없는 이미지와 7일 지난 빌드 캐시를 지운다. 실패해도 배포는 끝났으므로 알리기만 한다
    docker image prune -f > /dev/null || echo "이미지 정리 실패 (배포는 완료)" >&2
    docker builder prune -f --filter until=168h > /dev/null || echo "빌드 캐시 정리 실패 (배포는 완료)" >&2
    echo "배포 완료: $(git log --oneline -1)"
}

main "$@"
exit
