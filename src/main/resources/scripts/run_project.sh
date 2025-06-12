#!/bin/bash

JAVA_PATH=$1
PROJECT_DIR=$2
JAR_NAME=$3
PORT=$4
shift 4

ENV_VARS=("$@")

cd "$PROJECT_DIR" || exit 1

# Create temporary environment file
TMP_ENV_FILE=$(mktemp)
for var in "${ENV_VARS[@]}"; do
  echo "$var" >> "$TMP_ENV_FILE"
done

# Source environment variables
set -a
source "$TMP_ENV_FILE"
set +a

# Use log directory one level above the project directory
LOG_DIR="$(dirname "$PROJECT_DIR")/logs"

# Define log file path with timestamp
LOG_FILE="$LOG_DIR/app_$(date +%Y%m%d_%H%M%S).log"

# Run the Java application with --server.port
nohup "$JAVA_PATH/java" -jar "target/$JAR_NAME" --server.port="$PORT" >> "$LOG_FILE" 2>&1 &

# Store PID
PID=$!

# Clean up temp file
rm -f "$TMP_ENV_FILE"

# Create symlink to latest log file
ln -sf "$LOG_FILE" "$LOG_DIR/latest.log"

# Return PID
echo $PID
