compile_android_test_sources:
	./gradlew compileDebugAndroidTestSources -q

test_debug_all:
	./gradlew testDebugAll -q

enable_dated_version_name:
	./gradlew -q :app:enableDatedVersionName

distribute_debug:
	./gradlew bundleDebug appDistributionUploadDebug

pr_check: compile_android_test_sources test_debug_all

setup_local_mw:
	./gradlew libs:publishToMavenLocal

enable_analytics_for_debug:
	sed -i -e 's/config.enableAnalyticsForDebugBuilds=false/config.enableAnalyticsForDebugBuilds=true/g' analytics/gradle.properties

disable_analytics_for_debug:
	sed -i -e 's/config.enableAnalyticsForDebugBuilds=true/config.enableAnalyticsForDebugBuilds=false/g' analytics/gradle.properties

encryption:
	./scripts/common/encrypt.sh

decryption:
	./scripts/common/decrypt.sh