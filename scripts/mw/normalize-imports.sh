#!/usr/bin/env bash

echo -n "Starting script for normalizing imports"

echo -n "Normalizing imports... "

cd protocol/src/main/proto/ || exit

sed -i '' 's/pkg\/lib\/pb\/model\/protos\///g' events.proto
sed -i ''  's/pb\/protos\///g' events.proto

sed -i '' 's/pkg\/lib\/pb\/model\/protos\///g' commands.proto
sed -i '' 's/pb\/protos\///g' commands.proto

sed -i '' 's/pkg\/lib\/pb\/model\/protos\///g' changes.proto
sed -i '' 's/pb\/protos\///g' changes.proto

sed -i '' 's/pkg\/lib\/pb\/model\/protos\///g' localstore.proto
sed -i '' 's/pb\/protos\///g' localstore.proto

sed -i '' 's/pkg\/lib\/pb\/model\/protos\///g' snapshot.proto
sed -i '' 's/pb\/protos\///g' snapshot.proto

echo -n "Done normalizing imports."

echo -n ">>> Make sure to update mw version in libs.versions.toml >>>"

