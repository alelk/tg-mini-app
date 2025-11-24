#!/bin/bash
set -e
set -u

if [ ! -f .nextRelease.txt ]; then
  echo "Error: .nextRelease.txt not found"
  exit 1
fi

NEXT_RELEASE_VERSION=$(cat .nextRelease.txt)
export NEXT_RELEASE_VERSION

if [[ $NEXT_RELEASE_VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+ ]]; then
  echo "${NEXT_RELEASE_VERSION%%-*}" > app.version
else
  echo "No release published"
fi
