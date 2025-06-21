#!/bin/bash
GIT_URL=$1
BRANCH=$2
CLONE_DIR=$3
DEPLOY_TYPE=$4

EXIT_CODE=0
OUTPUT=""
STATUS="SUCCESS"
MESSAGE=""
DATA="[]"

TIMESTAMP=$(date '+%Y-%m-%d_%H-%M-%S')
DEPLOY_BRANCH=""

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
    --arg output "$OUTPUT" \
    --arg status "$STATUS" \
    --arg message "$MESSAGE" \
    --argjson data "$DATA" \
    --argjson exitCode "$EXIT_CODE" \
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

if [ -z "$GIT_URL" ] || [ -z "$BRANCH" ] || [ -z "$CLONE_DIR" ] || [ -z "$DEPLOY_TYPE" ]; then
  fail 1 "Missing required arguments: <GIT_URL> <BRANCH> <CLONE_DIR> <DEPLOY_TYPE>" ""
fi

rm -rf "$CLONE_DIR" 2>&1 | tee /tmp/script_output.log
mkdir -p "$CLONE_DIR" 2>&1 | tee -a /tmp/script_output.log

git clone --verbose --single-branch -b "$BRANCH" "$GIT_URL" "$CLONE_DIR" 2>&1 | tee -a /tmp/script_output.log
if [ ${PIPESTATUS[0]} -ne 0 ]; then
  fail 1 "Git clone failed" "$(cat /tmp/script_output.log)"
fi

cd "$CLONE_DIR" || fail 1 "Failed to cd into $CLONE_DIR" "$(cat /tmp/script_output.log)"

if [ "$DEPLOY_TYPE" == "CLASSIC" ]; then
  DEPLOY_BRANCH="deploy/$TIMESTAMP"
  git checkout -b "$DEPLOY_BRANCH" 2>&1 | tee -a /tmp/script_output.log
  if [ ${PIPESTATUS[0]} -ne 0 ]; then
    fail 1 "Failed to create deploy branch $DEPLOY_BRANCH" "$(cat /tmp/script_output.log)"
  fi

  git push origin "$DEPLOY_BRANCH" 2>&1 | tee -a /tmp/script_output.log
  if [ ${PIPESTATUS[0]} -ne 0 ]; then
    fail 1 "Failed to push deploy branch $DEPLOY_BRANCH" "$(cat /tmp/script_output.log)"
  fi

  MESSAGE="Cloned and created deploy branch $DEPLOY_BRANCH"
  DATA="[\"$DEPLOY_BRANCH\"]"
else
  DEPLOY_BRANCH="$BRANCH"
  git checkout "$DEPLOY_BRANCH" 2>&1 | tee -a /tmp/script_output.log
  if [ ${PIPESTATUS[0]} -ne 0 ]; then
    fail 1 "Failed to checkout existing branch $DEPLOY_BRANCH" "$(cat /tmp/script_output.log)"
  fi
  MESSAGE="Cloned and reused branch $DEPLOY_BRANCH for rollback"
  DATA="[\"$DEPLOY_BRANCH\"]"
fi

OUTPUT="$(cat /tmp/script_output.log)"
finish