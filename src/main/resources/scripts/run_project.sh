#!/bin/bash
JAVA_PATH=$1
SOURCE_DIR=$2
JAR_NAME=$3
PORT=$4
shift 4
ENV_VARS="$@"

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

JAR_DIR=$(dirname "$JAR_PATH")

# Logs directory next to source
LOGS_BASE_DIR="$SOURCE_DIR/../logs"
mkdir -p "$LOGS_BASE_DIR"

ENV_FILE="$JAR_DIR/.env"
LOG_FILE="$LOGS_BASE_DIR/app.log"

echo -n "" > "$ENV_FILE"
echo "---- Starting application ----" > "$LOG_FILE"

# Write and export env vars
for var in "$@"; do
  echo "$var" >> "$ENV_FILE"
done

while IFS= read -r line || [[ -n "$line" ]]; do
  [[ "$line" =~ ^# || -z "$line" ]] && continue
  export "$line"
  echo "Exported: $line" >> "$LOG_FILE"
done < "$ENV_FILE"

COMMAND="\"$JAVA_PATH/java\" -jar \"$JAR_PATH\" --server.port=$PORT"
echo "Executing command: $COMMAND" >> "$LOG_FILE"

# Run application
nohup bash -c "exec $COMMAND" >> "$LOG_FILE" 2>&1 &

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
