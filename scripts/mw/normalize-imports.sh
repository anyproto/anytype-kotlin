#!/usr/bin/env bash

echo "Starting script for normalizing imports"

echo "Normalizing imports... "

cd protocol/src/main/proto/ || exit

sed -i '' 's/pkg\/lib\/pb\/model\/protos\///g' events.proto
sed -i ''  's/pb\/protos\///g' events.proto

sed -i '' 's/pkg\/lib\/pb\/model\/protos\///g' commands.proto
sed -i '' 's/pb\/protos\///g' commands.proto

sed -i '' 's/pkg\/lib\/pb\/model\/protos\///g' changes.proto
sed -i '' 's/pb\/protos\///g' changes.proto

sed -i '' 's/pkg\/lib\/pb\/model\/protos\///g' localstore.proto
sed -i '' 's/pb\/protos\///g' localstore.proto

echo "Done normalizing imports."

echo ">>> Make sure to update mw version in libs.versions.toml >>>"

