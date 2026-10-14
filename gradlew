#!/bin/sh
# Bootstrap wrapper: downloads the pinned Gradle distribution and runs it.
set -eu
GRADLE_VERSION="8.9"
DIST_NAME="gradle-${GRADLE_VERSION}-bin"
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/manual"
DEST="$BASE/$DIST_NAME"
if [ ! -x "$DEST/bin/gradle" ]; then
  mkdir -p "$BASE"
  ARCHIVE="$BASE/$DIST_NAME.zip"
  URL="https://services.gradle.org/distributions/$DIST_NAME.zip"
  echo "Downloading Gradle $GRADLE_VERSION..."
  if command -v curl >/dev/null 2>&1; then curl -fL "$URL" -o "$ARCHIVE"
  elif command -v wget >/dev/null 2>&1; then wget "$URL" -O "$ARCHIVE"
  else echo "Install curl or wget, then retry." >&2; exit 1; fi
  unzip -q -o "$ARCHIVE" -d "$BASE"
  rm -f "$ARCHIVE"
fi
exec "$DEST/bin/gradle" "$@"
