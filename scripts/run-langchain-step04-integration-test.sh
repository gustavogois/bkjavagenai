#!/usr/bin/env bash
set -euo pipefail

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

if [[ -z "${OPENAI_API_KEY:-}" ]]; then
  echo "OPENAI_API_KEY is not set. Export it before running this script."
  exit 1
fi

./mvnw -Dmaven.repo.local=/tmp/m2 -Dtest=FlightControllerIntegrationTests test
