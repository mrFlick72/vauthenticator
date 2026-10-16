#!/usr/bin/env bash
# Replace the website files in a gh-pages checkout with a fresh build,
# keeping the Helm repository files that chart-releaser publishes there.
# Usage: sync-gh-pages.sh <dist-dir> <gh-pages-checkout-dir>
set -euo pipefail

dist="$(cd "$1" && pwd)"
target="$(cd "$2" && pwd)"

[ -f "$dist/index.html" ] || { echo "no index.html in $dist" >&2; exit 1; }
[ -d "$target/.git" ] || [ -f "$target/.git" ] || { echo "$target is not a git checkout" >&2; exit 1; }

cd "$target"
find . -mindepth 1 -maxdepth 1 \
  ! -name .git \
  ! -name index.yaml \
  ! -name '*.tgz' \
  ! -name artifacthub-repo.yml \
  ! -name CNAME \
  -exec rm -rf {} +

cp -R "$dist"/. "$target"/
touch "$target/.nojekyll"
