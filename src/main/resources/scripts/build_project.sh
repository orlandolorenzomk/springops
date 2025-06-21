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

# Wait up to 250 seconds for JAR file
for i in {1..250}; do
  JAR_COUNT=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "original*" | wc -l)
  [ "$JAR_COUNT" -gt 0 ] && break
  sleep 1
done

# Collect artifacts
JARS=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "original*" -exec basename {} \; | jq -R . | jq -s .)
DATA="$JARS"
OUTPUT="$BUILD_OUTPUT"
finish
