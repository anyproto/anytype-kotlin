compile_android_test_sources:
	./gradlew compileDebugAndroidTestSources -q

test_debug_all:
	./gradlew testDebugAll -q

pr_check: compile_android_test_sources test_debug_all