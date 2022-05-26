# Makes sure that android test compile
./gradlew compileDebugAndroidTestSources -q
# Run all unit tests for debug build
./gradlew testDebugAll -q