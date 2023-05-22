#!/usr/bin/env bash

REPO="anytypeio/go-anytype-middleware"
FILE="lib.tar.gz"
GITHUB="api.github.com"

echo "Enter your github token (check your github.properties file)"

read -r TOKEN

if [ "$TOKEN" = "" ]; then
  echo "ERROR: token is empty"
  exit 1
fi;

echo "Enter MW version without any prefix (for instance: 0.26.0)"

read -r MW_VERSION

if [ "$MW_VERSION" = "" ]; then
  echo "ERROR: mw version is empty"
  exit 1
fi;

version=`curl -H "Authorization: token $TOKEN" -H "Accept: application/vnd.github.v3+json" -sL https://$GITHUB/repos/$REPO/releases/tags/v$MW_VERSION | jq .`

tag=`echo $version | jq ".tag_name"`
asset_id=`echo $version | jq ".assets | map(select(.name | match(\"android_lib_\";\"i\")))[0].id"`

if [ "$asset_id" = "null" ]; then
  echo "ERROR: version not found"
  exit 1
fi;

printf "Version: $tag\n"
printf "Found asset: $asset_id\n"
echo -n "Downloading file... "
curl -sL -H 'Accept: application/octet-stream' https://$TOKEN:@$GITHUB/repos/$REPO/releases/assets/$asset_id > $FILE
printf "Done\n"

echo -n "Uncompressing... "

mkdir /tmp/lib

tar -zxf $FILE -C /tmp/lib/
printf "Done\n"

printf "Preparing files\n"

# Moving proto files to protocol module
mkdir -p libs/protobuf/protos
mv /tmp/lib/protobuf/protos/* protocol/src/main/proto


# Clearing temp folder
rm -rf /tmp/lib
rm -rf $FILE

# For Android app internal usage, we don't need the following protos:
rm -rf protocol/src/main/proto/service.proto
rm -rf protocol/src/main/proto/block.proto
rm -rf protocol/src/main/proto/file.proto
rm -rf protocol/src/main/proto/snapshot.proto
rm -rf protocol/src/main/proto/migration.proto

echo "Done with downloading protobuf files!"

echo ">>> Make sure to update mw version to version $tag in libs.versions.toml >>>"