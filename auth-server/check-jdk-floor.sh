#!/usr/bin/env bash
# Fails when the JDK inside a docker image is older than the given floor.
# Usage: check-jdk-floor.sh <image> <floor>   e.g. check-jdk-floor.sh vauthenticator:latest 25.0.5.1
#
# The floor is the lowest JDK build carrying the fixes for known JDK CVEs:
# 25.0.5.1 fixes Java4Shell (CVE-2026-114938) and drops the distrusted eMudhra intermediates (#447).
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "usage: $0 <image> <floor>" >&2
  exit 2
fi

image="$1"
floor="$2"

version=$(docker run --rm --entrypoint java "$image" -XshowSettings:properties -version 2>&1 \
  | sed -n 's/^ *java\.runtime\.version = \([0-9][0-9.]*\).*/\1/p')

if [[ -z "$version" ]]; then
  echo "could not read java.runtime.version from $image" >&2
  exit 1
fi

lowest=$(printf '%s\n%s\n' "$floor" "$version" | sort -V | head -n1)
if [[ "$lowest" != "$floor" ]]; then
  echo "JDK $version in $image is older than the required floor $floor" >&2
  exit 1
fi

echo "JDK $version in $image meets the floor $floor"
