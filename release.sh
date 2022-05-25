#!/usr/bin/env bash

TYPES=(patch minor major)

RELEASE_TYPE=$1

if ! [ "$RELEASE_TYPE" = "${TYPES[0]}" ] && ! [ "$RELEASE_TYPE" = "${TYPES[1]}" ] && ! [ "$RELEASE_TYPE" = "${TYPES[2]}" ]; then
  echo "Please provide release type: major, minor or patch"
  exit 1
fi;

read -p "Hi there. Do you want to make a release? " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    echo -n "Checking out develop branch... "
    git checkout develop
    echo Done

    read -p "Are you sure you want to release a new version? (Y/N)  " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]
    then
        echo
        echo -n "Incrementing app version... "

        if [ "$RELEASE_TYPE" = "${TYPES[0]}" ]; then
          ./gradlew -q :app:incrementVersionPatch
        fi;

        if [ "$RELEASE_TYPE" = "${TYPES[1]}" ]; then
          ./gradlew -q :app:incrementVersionMinor
        fi;

        if [ "$RELEASE_TYPE" = "${TYPES[2]}" ]; then
          ./gradlew -q :app:incrementVersionMajor
        fi;

        echo Done.
        echo

        PATCH_VERSION=`grep 'version.versionPatch=' app/gradle.properties | sed 's/version.versionPatch=//'`
        MINOR_VERSION=`grep 'version.versionMinor=' app/gradle.properties | sed 's/version.versionMinor=//'`
        MAJOR_VERSION=`grep 'version.versionMajor=' app/gradle.properties | sed 's/version.versionMajor=//'`
        VERSION=${MAJOR_VERSION}.${MINOR_VERSION}.${PATCH_VERSION}

        echo New app version: ${VERSION}
        echo

        echo -n "Staging changes... "

        git add app/gradle.properties

        echo Done

        echo -n "Commiting changes... "

        git commit -m "Release commit: ${VERSION}"

        echo Done.

        git tag -a ${VERSION} -m "Release tag: ${VERSION}"

        echo -n "Pushing changes to remote repository..."

        git push
        git push --tags

        echo Done
        echo
        echo Congratulations! You have just released a new version of the app: ${VERSION}!

    else
        echo Skipping release.. Done.
    fi
else
    echo Skipping release
fi

echo
echo Goodbye!
