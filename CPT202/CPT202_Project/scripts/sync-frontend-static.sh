#!/usr/bin/env sh

set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
FRONTEND_DIST="$REPO_ROOT/frontend/dist"
STATIC_DIR="$REPO_ROOT/src/main/resources/static"

if [ ! -d "$FRONTEND_DIST" ]; then
  echo "Frontend build output not found: $FRONTEND_DIST" >&2
  echo "Run 'npm run build' in the frontend directory first." >&2
  exit 1
fi

mkdir -p "$STATIC_DIR"
find "$STATIC_DIR" -mindepth 1 -maxdepth 1 -exec rm -rf {} +
cp -R "$FRONTEND_DIST"/. "$STATIC_DIR"/
