#!/usr/bin/env bash

TOKEN=$1

PROPERTY_PATH="middleware.path"
PROPERTY_VERSION="middleware.version"
LIBRARY_PATH="libs/lib.aar"

REPO="anytypeio/go-anytype-middleware"
FILE="lib.tar.gz"
GITHUB="api.github.com"

if [ "$TOKEN" = "" ]; then
  echo "ERROR: token is empty"
  exit 1
fi;

version=`curl -H "Authorization: token $TOKEN" -H "Accept: application/vnd.github.v3+json" -sL https://$GITHUB/repos/$REPO/releases | jq ".[0]"`
tag=`echo $version | jq ".tag_name"`
asset_id=`echo $version | jq ".assets | map(select(.name | match(\"android_lib_\";\"i\")))[0].id"`

if [ "$asset_id" = "" ]; then
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

mkdir -p libs/
mv /tmp/lib/lib.aar libs/

rm -rf /tmp/lib
rm -rf $FILE

printf "Done with downloading the library!"

echo "Creating configuration file..."

rm -rf configuration.properties

touch configuration.properties

echo "$PROPERTY_PATH=$LIBRARY_PATH" >>configuration.properties
echo "$PROPERTY_VERSION=$tag" >>configuration.properties

echo Configuration file has been successfully created. It contains following properties:

cat configuration.properties

echo Update finished.
echo Now you can rebuild your project. Good luck!