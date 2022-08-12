compile_android_test_sources:
	./gradlew compileDebugAndroidTestSources -q

test_debug_all:
	./gradlew testDebugAll -q

enable_dated_version_name:
	./gradlew -q :app:enableDatedVersionName

distribute_debug:
	./gradlew bundleDebug appDistributionUploadDebug

enable_analytics_for_debug:
	./gradlew :analytics:enableAnalyticsForDebugBuilds

pr_check: compile_android_test_sources test_debug_all

