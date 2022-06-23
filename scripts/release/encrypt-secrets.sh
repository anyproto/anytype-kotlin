#!/usr/bin/env bash

encrypt() {
  PASSPHRASE=$1
  INPUT=$2
  OUTPUT=$3
  gpg --batch --yes --passphrase="$PASSPHRASE" --cipher-algo AES256 --symmetric --output $OUTPUT $INPUT
}

if [[ ! -z "$ENCRYPT_KEY" ]]; then
  encrypt ${ENCRYPT_KEY} scripts/release/app-release.jks scripts/release/app-release.gpg
else
  echo "ENCRYPT_KEY is empty"
fi