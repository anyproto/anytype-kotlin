#!/usr/bin/env bash

TOKEN=$1
USER=$2

GITHUB_USER_PROPERTY="gpr.usr"
GITHUB_KEY_PROPERTY="gpr.key"

if [ "$TOKEN" = "" ]; then
  echo "ERROR: token is empty"
  exit 1
fi;

if [ "$USER" = "" ]; then
  echo "ERROR: user is empty"
  exit 1
fi;

rm -rf github.properties
touch github.properties

echo "$GITHUB_USER_PROPERTY=$USER" >> github.properties
echo "$GITHUB_KEY_PROPERTY=$TOKEN" >> github.properties