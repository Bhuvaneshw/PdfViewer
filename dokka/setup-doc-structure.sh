#!/usr/bin/env bash
set -euo pipefail

############################################
# Usage:
# ./setup-doc-structure.sh <latest_version> <build_root> <publish_root>
#
# Example:
# ./setup-doc-structure.sh 1.1.0 build/docs docs/v in the workflow
# ./setup-doc-structure.sh 1.1.0 ../build/docs ../docs/v locally
############################################

LATEST_VERSION="${1:?Missing latest version}"
BUILD_ROOT="${2:?Missing build root}"
PUBLISH_ROOT="${3:?Missing publish root}"

LATEST_BUILD_DIR="$BUILD_ROOT/$LATEST_VERSION"
OLDER_DIR="$LATEST_BUILD_DIR/older"

echo "----------------------------------------"
echo "Restructuring Dokka output"
echo "Latest version: $LATEST_VERSION"
echo "Build root: $BUILD_ROOT"
echo "Publish root: $PUBLISH_ROOT"
echo "----------------------------------------"

# -------------------------------------------------
# 1. Ensure publish root exists
# -------------------------------------------------
mkdir -p "$PUBLISH_ROOT"

# -------------------------------------------------
# 2. Copy latest version normally
# -------------------------------------------------
echo "Copying latest version..."
rm -rf "$PUBLISH_ROOT/$LATEST_VERSION"
mkdir -p "$PUBLISH_ROOT/$LATEST_VERSION"
cp -r "$LATEST_BUILD_DIR/"* "$PUBLISH_ROOT/$LATEST_VERSION/"

# -------------------------------------------------
# 3. Move all versions from older/ into publish root
# -------------------------------------------------
if [ -d "$OLDER_DIR" ]; then
  echo "Moving older versions to publish root..."

  for ver_path in "$OLDER_DIR"/*; do
    [ -d "$ver_path" ] || continue

    ver_name=$(basename "$ver_path")

    echo "Processing older version: $ver_name"

    rm -rf "$PUBLISH_ROOT/$ver_name"
    mkdir -p "$PUBLISH_ROOT/$ver_name"
    cp -r "$ver_path/"* "$PUBLISH_ROOT/$ver_name/"
  done
else
  echo "No older directory found — nothing to move"
fi

# -------------------------------------------------
# 4. Fix links inside ALL index.html files
# -------------------------------------------------
echo "Rewriting index.html links..."

find "$PUBLISH_ROOT" -type f -name "index.html" | while read -r file; do
  echo "Fixing: $file"

  # Replace 'older/' -> '../'
  sed -i 's|older/|../|g' "$file"

  # Replace '../../' -> '../<latest_version>/'
  sed -i "s|\.\./\.\./|../$LATEST_VERSION/|g" "$file"
done

echo "----------------------------------------"
echo "Restructure complete"
echo "----------------------------------------"
