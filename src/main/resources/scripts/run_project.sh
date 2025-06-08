#!/bin/bash

JAVA_PATH=$1
PROJECT_DIR=$2
JAR_NAME=$3
shift 3

ENV_VARS=("$@")

cd "$PROJECT_DIR" || exit 1

TMP_ENV_FILE=".temp_env"
> "$TMP_ENV_FILE"
for var in "${ENV_VARS[@]}"; do
  echo "$var" >> "$TMP_ENV_FILE"
done

set -a
source "$TMP_ENV_FILE"
set +a

nohup "$JAVA_PATH/java" -jar "target/$JAR_NAME" > "target/app.log" 2>&1 &

PID=$!
rm -f "$TMP_ENV_FILE"

echo $PID
