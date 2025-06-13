#!/bin/bash

JAVA_BIN_PATH=$1
MAVEN_BIN_PATH=$2
PROJECT_DIR=$3
JAVA_VERSION=$4

EXIT_CODE=0
STATUS="SUCCESS"
MESSAGE="Build completed successfully"
OUTPUT=""
DATA="[]"

function fail() {
  EXIT_CODE=$1
  STATUS="FAILURE"
  MESSAGE="$2"
  DATA="[]"
  OUTPUT="$3"
  finish
}

function finish() {
  JSON=$(jq -n \
    --argjson exitCode "$EXIT_CODE" \
    --arg output "$OUTPUT" \
    --arg status "$STATUS" \
    --arg message "$MESSAGE" \
    --argjson data "$DATA" \
    '{
      exitCode: $exitCode,
      output: $output,
      status: $status,
      message: $message,
      data: $data
    }')

  echo "springops-result=${JSON}"
  exit "$EXIT_CODE"
}


if [ -z "$JAVA_BIN_PATH" ] || [ -z "$MAVEN_BIN_PATH" ] || [ -z "$PROJECT_DIR" ] || [ -z "$JAVA_VERSION" ]; then
  fail 1 "Usage: $0 <JAVA_BIN_PATH> <MAVEN_BIN_PATH> <PROJECT_DIR> <JAVA_VERSION>" ""
fi

cd "$PROJECT_DIR" || fail 1 "Failed to enter project directory: $PROJECT_DIR" ""

JAVA_HOME="${JAVA_BIN_PATH%/bin}"

BUILD_OUTPUT=$(JAVA_HOME="$JAVA_HOME" "$MAVEN_BIN_PATH/mvn" clean install --no-transfer-progress -Dmaven.compiler.release="$JAVA_VERSION" -DskipTests 2>&1)
STATUS_CODE=$?

if [ $STATUS_CODE -ne 0 ]; then
  fail $STATUS_CODE "Build failed" "$BUILD_OUTPUT"
fi

# Collect artifacts
JARS_ARRAY=()
for f in target/*.jar; do
  [ -e "$f" ] || continue
  JARS_ARRAY+=("\"$(basename "$f")\"")
done

DATA="[${JARS_ARRAY[*]}]"
OUTPUT="$BUILD_OUTPUT"
finish
