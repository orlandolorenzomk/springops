#!/bin/bash

JAVA_BIN_PATH=$1
MAVEN_BIN_PATH=$2
PROJECT_DIR=$3
JAVA_VERSION=$4

if [ -z "$JAVA_BIN_PATH" ] || [ -z "$MAVEN_BIN_PATH" ] || [ -z "$PROJECT_DIR" ] || [ -z "$JAVA_VERSION" ]; then
  echo "{\"success\":false,\"error\":\"Usage: $0 <JAVA_BIN_PATH> <MAVEN_BIN_PATH> <PROJECT_DIR> <JAVA_VERSION>\"}"
  exit 1
fi

cd "$PROJECT_DIR" || {
  echo "{\"success\":false,\"error\":\"Failed to enter project directory\"}"
  exit 1
}

JAVA_HOME="${JAVA_BIN_PATH%/bin}"

OUTPUT=$(JAVA_HOME="$JAVA_HOME" "$MAVEN_BIN_PATH/mvn" clean install --no-transfer-progress -Dmaven.compiler.release="$JAVA_VERSION" -DskipTests 2>&1)
STATUS=$?

if [ $STATUS -ne 0 ]; then
  ESCAPED_OUTPUT=$(echo "$OUTPUT" | python3 -c 'import json,sys; print(json.dumps(sys.stdin.read()))')
  echo "{\"success\":false,\"error\":\"Build failed\",\"details\":$ESCAPED_OUTPUT}"
  exit $STATUS
fi

# For Linux GNU find
if find --version >/dev/null 2>&1; then
  JARS=$(find target -maxdepth 1 -type f -name "*.jar" -printf '"%f",' | sed 's/,$//')
else
  # For macOS/BSD find fallback
  JARS_ARRAY=()
  for f in target/*.jar; do
    [ -e "$f" ] || continue
    JARS_ARRAY+=("\"$(basename "$f")\"")
  done
  JARS=$(IFS=,; echo "${JARS_ARRAY[*]}")
fi

echo "{\"success\":true,\"artifacts\":[$JARS]}"
