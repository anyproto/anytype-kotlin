#!/usr/bin/env bash

decrypt() {
  PASSPHRASE=$1
  INPUT=$2
  OUTPUT=$3
  gpg --quiet --batch --yes --decrypt --passphrase="$PASSPHRASE" --output $OUTPUT $INPUT
}

if [[ ! -z "$ENCRYPT_KEY" ]]; then
  decrypt ${ENCRYPT_KEY} scripts/distribution/anytype-debug-service-account-key.gpg scripts/distribution/anytype-debug-service-account-key.json
else
  echo "ENCRYPT_KEY is empty"
fi