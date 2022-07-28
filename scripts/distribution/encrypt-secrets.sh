#!/usr/bin/env bash

encrypt() {
  PASSPHRASE=$1
  INPUT=$2
  OUTPUT=$3
  gpg --batch --yes --passphrase="$PASSPHRASE" --cipher-algo AES256 --symmetric --output $OUTPUT $INPUT
}

if [[ ! -z "$ENCRYPT_KEY" ]]; then
  encrypt ${ENCRYPT_KEY} scripts/distribution/anytype-debug-service-account-key.json scripts/distribution/anytype-debug-service-account-key.gpg
else
  echo "ENCRYPT_KEY is empty"
fi