#!/usr/bin/env bash
set -euo pipefail

JAVA_PATH=$1
SOURCE_DIR=$2
JAR_NAME=$3
PORT=$4
JAVA_MIN=${5:-512m}
JAVA_MAX=${6:-1024m}
shift 6
ENV_VARS=("$@")

EXIT_CODE=0
OUTPUT=""
STATUS="SUCCESS"
MESSAGE=""
DATA="[]"

function fail() {
  EXIT_CODE=$1; STATUS="FAILURE"; MESSAGE="$2"; DATA="[]"; OUTPUT="$3"; finish
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

LOGS_BASE_DIR="$SOURCE_DIR/../logs"
mkdir -p "$LOGS_BASE_DIR"
LOG_FILE="$LOGS_BASE_DIR/app.log"

echo "---- Starting application ----" > "$LOG_FILE"
echo "Using Xms=$JAVA_MIN, Xmx=$JAVA_MAX" >> "$LOG_FILE"

# Export environment variables
ENV_EXPORTS=""
for var in "${ENV_VARS[@]}"; do
  echo "Exporting: $var" >> "$LOG_FILE"
  ENV_EXPORTS+="export $var; "
done

RUN_CMD="$ENV_EXPORTS \"$JAVA_PATH/java\" -Xms$JAVA_MIN -Xmx$JAVA_MAX -jar \"$JAR_PATH\" --server.port=$PORT"
echo "Executing: $RUN_CMD" >> "$LOG_FILE"

nohup bash -c "$RUN_CMD" >> "$LOG_FILE" 2>&1 &
PID=$!
sleep 2

if ps -p $PID > /dev/null; then
  MESSAGE="Application started successfully"
  DATA="[\"$JAR_NAME\", $PID]"
  echo "Application started with PID $PID" >> "$LOG_FILE"
else
  fail 1 "Application failed to start" "$(cat "$LOG_FILE")"
fi

finish
