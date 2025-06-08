#!/bin/bash
GIT_URL=$1
BRANCH=$2
CLONE_DIR=$3

if [ -z "$GIT_URL" ] || [ -z "$BRANCH" ] || [ -z "$CLONE_DIR" ]; then
  echo "Usage: $0 <GIT_URL> <BRANCH> <CLONE_DIR>"
  exit 1
fi

rm -rf "$CLONE_DIR"
mkdir -p "$CLONE_DIR"
cd "$CLONE_DIR" || exit 1

git clone --verbose --single-branch -b "$BRANCH" "$GIT_URL" .
