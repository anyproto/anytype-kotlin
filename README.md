# Anytype.io - Mobile

### Build project

##### Prerequisites

- Download `anytype/anytype-android` (private) repository from Github
  
- Create `github.properties` file in root project folder:

```
gpr.usr=GITHUB_USER_ID
gpr.key=GITHUB_PERSONAL_ACCESS_TOKEN
```

Your Github ID (starting with '#' character) can be found [here](https://caius.github.io/github_id/). As to your personal Github access token, you can generate it in `Developer settings` in your profile settings on Github. 

- Create `apikeys.properties` file in root project folder:

```
amplitude.debug="AMPLITUDE_DEBUG_KEY"
amplitude.release="AMPLITUDE_RELEASE_KEY"
sentry_dsn="SENTRY_DSN_KEY"
```

Then build project.

### IDE setup (optional) 

Based on your IDE setup, you might experience problems while accessing/importing auto-generated Kotlin classes from `protocol` module. These classes are currently declared in very large files, which IDE might not process as expected. If this is your case, go to your Android Studio _Help_ section, select _Edit Custom Properties_ option. There you should set `idea.max.intellisense.filesize` property as follows:

```
idea.max.intellisense.filesize=3500
```

### Setup your Firebase account for Anytype

We're using *Firebase App Distribution* + *Firebase Crashlytics*. We have two separate projects: one for `debug` builds (which we distribute mostly for our Q&A team), another one for `release` builds.

### Install the latest Anytype Android release: 

From [Google Play](https://play.google.com/store/apps/details?id=com.anytypeio.anytype).

Or [download](https://download.anytype.io/) an apk for your device architecture from our website.

### Join our community & telegram channels

[Official Anytype community](https://community.anytype.io/).

[Follow what community writes about our Android client](https://community.anytype.io/tag/Android).

[Join our Android Testers telegram channel](https://t.me/+vEb8COFY7rY5Mzli).

### Useful links

[Our tech change log](https://github.com/anytypeio/android-anytype/blob/develop/CHANGELOG.md).

[Anytype Android app dependencies](https://github.com/anytypeio/android-anytype/blob/develop/gradle/libs.versions.toml).

### Conventions

##### PR naming: 
```
{TASK-ID} {APP AREA} | {NATURE OF CHANGE: Fix, Enhancement, Feature, Design, Documentation} | {CONCISE DESCRIPTION OF WHAT HAS BEEN DONE}
```

App area can be `App`, `Editor`, `Sets`, `Relations`, `Auth`, `Settings`, `Analytics`, `Tech` (CI,
DI, scripting, etc.), etc.

Example: *Editor | Fix | Show meaningful message when failed to open file by an existing
application*

##### Git branch naming

```
{TASK_ID or TASK_NUMBER}-ConciseDescription
```

##### Flags

```
com.anytype.ci=true - for CI/CD pipeline
```

### Updating MW (basics)

1. Run the following command in Terminal or Makefile:

    ```
    make update_mw
    ```

2. Make sure your proto files located in `protocol/main/proto/` compile.
3. Make sure to update `middlewareVersion` version in `libs.versions.toml`.
