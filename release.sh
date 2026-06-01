#!/usr/bin/env bash
set -euo pipefail

VERSION="$1"

if [ -z "$VERSION" ]; then
  echo "Usage: ./release.sh <version>"
  echo "Example: ./release.sh 0.0.1"
  exit 1
fi

if ! command -v gh &> /dev/null; then
  echo "Error: gh (GitHub CLI) is required. Install it from https://cli.github.com/"
  exit 1
fi

git diff --quiet || { echo "Error: working tree is dirty. Commit or stash changes first."; exit 1; }
git fetch origin main
git rev-list "HEAD..origin/main" | grep -q . && { echo "Error: local main is behind origin/main. Pull first."; exit 1; }

gh release create "v$VERSION" \
  --title "v$VERSION" \
  --generate-notes

echo "Release v$VERSION created. The CI workflow (maven-release.yml) will now:"
echo "  1. Set version to $VERSION in pom.xml"
echo "  2. Commit and push the version bump"
echo "  3. Deploy $VERSION to Maven Central"
echo "  4. Bump to next SNAPSHOT and commit"
