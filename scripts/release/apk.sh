#!/bin/bash

# Script for preparing Android Manifest for APK distribution.

# Define the file to be modified
FILE="app/src/main/AndroidManifest.xml"

# Check if the system is macOS or Linux
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS (sed requires an empty string argument for in-place editing)
    sed -i '' 's|<!--    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />-->|<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />|' "$FILE"
    sed -i '' 's|<!--    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />-->|<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />|' "$FILE"
else
    # Linux (sed works without the empty string argument for in-place editing)
    sed -i 's|<!--    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />-->|<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />|' "$FILE"
    sed -i 's|<!--    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />-->|<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />|' "$FILE"
fi

echo "Lines uncommented successfully."