#!/bin/bash
JAVA_PATH=$1
SOURCE_DIR=$2
JAR_NAME=$3
PORT=$4
ENV_VARS=$5

EXIT_CODE=0
OUTPUT=""
STATUS="SUCCESS"
MESSAGE=""
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

if [ -z "$JAVA_PATH" ] || [ -z "$SOURCE_DIR" ] || [ -z "$JAR_NAME" ] || [ -z "$PORT" ]; then
  fail 1 "Missing arguments: JAVA_PATH, SOURCE_DIR, JAR_NAME, PORT" ""
fi

cd "$SOURCE_DIR" || fail 1 "Failed to cd into $SOURCE_DIR" ""

JAR_PATH=$(find "$SOURCE_DIR" -name "$JAR_NAME" | head -n 1)
if [ ! -f "$JAR_PATH" ]; then
  fail 1 "Jar file $JAR_NAME not found in $SOURCE_DIR" ""
fi

COMMAND="$ENV_VARS $JAVA_PATH/java -jar $JAR_PATH --server.port=$PORT"
nohup bash -c "$COMMAND" > "$SOURCE_DIR/app.log" 2>&1 &

PID=$!
sleep 2

if ps -p $PID > /dev/null; then
  MESSAGE="Application started successfully"
  DATA="[\"$JAR_NAME\", $PID]"
else
  fail 1 "Application failed to start" "$(cat "$SOURCE_DIR/app.log")"
fi

finish
