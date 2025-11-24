#!/bin/bash
set -e
set -u

if [ ! -f .nextRelease.txt ]; then
  echo "Error: .nextRelease.txt not found"
  exit 1
fi

NEXT_RELEASE_VERSION=$(cat .nextRelease.txt)
export NEXT_RELEASE_VERSION

echo "Building project..."
./gradlew build

if [[ $NEXT_RELEASE_VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Publishing $NEXT_RELEASE_VERSION as release"
  echo "$NEXT_RELEASE_VERSION" > app.version
  ./gradlew publishAllPublicationsToGitHubPackagesRepository
elif [[ $NEXT_RELEASE_VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+-rc.+$ ]]; then
  echo "Publishing $NEXT_RELEASE_VERSION as pre-release"
  echo "$NEXT_RELEASE_VERSION" > app.version
  ./gradlew publishAllPublicationsToGitHubPackagesRepository
elif [[ $NEXT_RELEASE_VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+-.+$ ]]; then
  echo "Publishing $NEXT_RELEASE_VERSION as snapshot"
  echo "${NEXT_RELEASE_VERSION%-*}-SNAPSHOT" > app.version
  ./gradlew publishAllPublicationsToGitHubPackagesRepository
else
  echo "No release published"
fi
