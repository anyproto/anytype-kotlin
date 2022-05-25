#!/usr/bin/env bash

TOKEN=$1
USER=$2
AMPLITUDE_RELEASE_KEY=$3
AMPLITUDE_DEBUG_KEY=$4

GITHUB_USER_PROPERTY="gpr.usr"
GITHUB_KEY_PROPERTY="gpr.key"
AMPLITUDE_DEBUG_PROPERTY="amplitude.debug"
AMPLITUDE_RELEASE_PROPERTY="amplitude.release"

if [ "$TOKEN" = "" ]; then
  echo "ERROR: token is empty"
  exit 1
fi;

if [ "$USER" = "" ]; then
  echo "ERROR: user is empty"
  exit 1
fi;

if [ "$AMPLITUDE_RELEASE_KEY" = "" ]; then
  echo "ERROR: amplitude.release is empty"
  exit 1
fi;

if [ "$AMPLITUDE_DEBUG_KEY" = "" ]; then
  echo "ERROR: amplitude.debug is empty"
  exit 1
fi;

rm -rf github.properties
touch github.properties

echo "$GITHUB_USER_PROPERTY=$USER" >> github.properties
echo "$GITHUB_KEY_PROPERTY=$TOKEN" >> github.properties

rm -rf apikeys.properties
touch apikeys.properties

echo "$AMPLITUDE_DEBUG_PROPERTY=\"$AMPLITUDE_DEBUG_KEY\"" >> apikeys.properties
echo "$AMPLITUDE_RELEASE_PROPERTY=\"$AMPLITUDE_RELEASE_KEY\"" >> apikeys.properties