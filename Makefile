compile_android_test_sources:
	./gradlew compileDebugAndroidTestSources -q

test_debug_all:
	./gradlew testDebugAll -q

distribute_debug:
	./gradlew bundleDebug appDistributionUploadDebug -Pversion.useDatedVersionName=true

distribute_debug_with_postfix:
	./gradlew bundleDebug appDistributionUploadDebug -Pversion.useDatedVersionName=true -Pversion.versionPostfix=$(POSTFIX)

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

download_mw_artefacts_custom:
	./scripts/mw/update-mw-custom.sh

normalize_mw_imports:
	./scripts/mw/normalize-imports.sh

clean_protos:
	./scripts/mw/clean-protos.sh

# Update mw when new release is ready
update_mw: download_mw_artefacts normalize_mw_imports clean_protos

# Update mw from custom build (download only library, you have to update your proto files manually)
update_mw_custom: download_mw_artefacts_custom

prepare_app_manifest_for_release_apk:
	./scripts/release/apk.sh


# WORKTREE MANAGEMENT
# wtlist              List all worktrees
# wtadd <branch>      Add worktree for existing branch + copy configs
# wtnew <branch>      Create new branch + worktree + copy configs
# wtrm  <branch>      Remove worktree
# For shell navigation: cd $(./scripts/wt.sh path <branch>)

.PHONY: wtlist wtadd wtnew wtrm

# Capture positional arg for worktree commands (e.g. make wtnew mybranch)
ifneq ($(filter wtadd wtnew wtrm,$(firstword $(MAKECMDGOALS))),)
  WT_BRANCH := $(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))
  $(eval $(WT_BRANCH):;@:)
endif

wtlist:
	./scripts/wt.sh list

wtadd:
	@[ -n "$(WT_BRANCH)" ] || (echo "Usage: make wtadd <branch>" && exit 1)
	./scripts/wt.sh add $(WT_BRANCH)

wtnew:
	@[ -n "$(WT_BRANCH)" ] || (echo "Usage: make wtnew <branch>" && exit 1)
	./scripts/wt.sh new $(WT_BRANCH)

wtrm:
	@[ -n "$(WT_BRANCH)" ] || (echo "Usage: make wtrm <branch>" && exit 1)
	./scripts/wt.sh rm $(WT_BRANCH)
