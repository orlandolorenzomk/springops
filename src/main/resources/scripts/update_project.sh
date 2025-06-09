#!/bin/bash
GIT_URL=$1
BRANCH=$2
CLONE_DIR=$3

# Initialize JSON result
RESULT_JSON=$(mktemp)

# Function to output JSON result
output_result() {
  local success=$1
  local branch=$2
  echo "{\"success\": $success, \"deployBranch\": \"$branch\"}" > $RESULT_JSON
  cat $RESULT_JSON
  rm -f $RESULT_JSON
  exit $([ "$success" = "true" ] && echo 0 || echo 1)
}

if [ -z "$GIT_URL" ] || [ -z "$BRANCH" ] || [ -z "$CLONE_DIR" ]; then
  echo "Usage: $0 <GIT_URL> <BRANCH> <CLONE_DIR>"
  output_result "false" ""
fi

rm -rf "$CLONE_DIR"
mkdir -p "$CLONE_DIR"

# Clone into the specified directory
git clone --verbose --single-branch -b "$BRANCH" "$GIT_URL" "$CLONE_DIR"
if [ $? -ne 0 ]; then
  output_result "false" ""
fi

cd "$CLONE_DIR" || output_result "false" ""

TIMESTAMP=$(date '+%Y-%m-%d_%H-%M-%S')
DEPLOY_BRANCH="deploy/$TIMESTAMP"

git checkout -b "$DEPLOY_BRANCH"
if [ $? -ne 0 ]; then
  output_result "false" ""
fi

output_result "true" "$DEPLOY_BRANCH"