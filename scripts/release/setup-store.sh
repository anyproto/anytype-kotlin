#!/usr/bin/env bash

KEY_ALIAS=$1
KEY_PWD=$2
STORE_PWD=$3

rm -rf signing.properties
touch signing.properties

echo "RELEASE_KEY_ALIAS=$KEY_ALIAS" >> signing.properties
echo "RELEASE_KEY_PASSWORD=$KEY_PWD" >> signing.properties
echo "RELEASE_STORE_PASSWORD=$STORE_PWD" >> signing.properties