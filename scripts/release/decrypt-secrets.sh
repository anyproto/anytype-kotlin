#!/usr/bin/env bash

decrypt() {
  PASSPHRASE=$1
  INPUT=$2
  OUTPUT=$3
  gpg --quiet --batch --yes --decrypt --passphrase="$PASSPHRASE" --output $OUTPUT $INPUT
}

if [[ ! -z "$ENCRYPT_KEY" ]]; then
  decrypt ${ENCRYPT_KEY} scripts/release/app-release.gpg scripts/release/app-release.jks
else
  echo "ENCRYPT_KEY is empty"
fi
