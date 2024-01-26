#!/usr/bin/env bash

REPO="anyproto/anytype-heart"
FILE="lib.aar"

echo "Enter custom MW build name:"

read -r MW_VERSION

if [ "$MW_VERSION" = "" ]; then
  echo "ERROR: mw version is empty"
  exit 1
fi;

echo "Enter your github token (you probably have in github.properties file in the project root folder):"

read -r GITHUB_TOKEN

if [ "$GITHUB_TOKEN" = "" ]; then
  echo "ERROR: github token is empty"
  exit 1
fi;

URL="https://maven.pkg.github.com/anyproto/anytype-heart/io.anyproto/anytype-heart-android/${MW_VERSION}/anytype-heart-android-${MW_VERSION}.aar"

echo

echo "Downloading URL: $URL"

echo

curl "${URL}" \
  -H "Authorization: Bearer $GITHUB_TOKEN" -L --fail > $FILE

if [ "$?" -gt 0 ]
then
  echo
  echo "Error downloading file. Exiting."
  exit
fi
  echo
  echo "Done downloading!"

  echo

  mv lib.aar libs/

  echo "What you need to do next:"
  echo
  echo "1. Make sure to update mw version with your custom build version in libs.versions.toml!"
  echo "2. Make sure to update custom proto files!"
  echo "3. Make sure to run make setup_local_mw in your terminal!"
  echo
  echo "Script finished."