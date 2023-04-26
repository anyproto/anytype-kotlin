compile_android_test_sources:
	./gradlew compileDebugAndroidTestSources -q

test_debug_all:
	./gradlew testDebugAll -q

enable_dated_version_name:
	./gradlew -q :app:enableDatedVersionName

distribute_debug:
	./gradlew bundleDebug appDistributionUploadDebug

pr_check: compile_android_test_sources test_debug_all

enable_analytics_for_debug:
	sed -i -e 's/config.enableAnalyticsForDebugBuilds=false/config.enableAnalyticsForDebugBuilds=true/g' analytics/gradle.properties

disable_analytics_for_debug:
	sed -i -e 's/config.enableAnalyticsForDebugBuilds=true/config.enableAnalyticsForDebugBuilds=false/g' analytics/gradle.properties

encryption:
	./scripts/common/encrypt.sh

decryption:
	./scripts/common/decrypt.sh


# MIDDLEWARE INTEGRATION

# Use this only for custom (not yet released) middleware library.
setup_local_mw:
	./gradlew libs:publishToMavenLocal

download_mw_artefacts:
	./scripts/mw/update-mw.sh

normalize_mw_imports:
	./scripts/mw/normalize-imports.sh

# Update mw when new release is ready
update_mw: download_mw_artefacts normalize_mw_imports