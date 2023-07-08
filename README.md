# Anytype

Official Anytype client for Android.

## Build project

### Prerequisites

- Clone this repository
  
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

## Useful links

[Our tech change log](https://github.com/anyproto/anytype-kotlin/blob/main/CHANGELOG.md)

[Anytype Android app dependencies](https://github.com/anyproto/anytype-kotlin/blob/main/gradle/libs.versions.toml)

## Conventions

### PR naming
```
{TASK-ID} {APP AREA} | {NATURE OF CHANGE: Fix, Enhancement, Feature, Design, Documentation} | {CONCISE DESCRIPTION OF WHAT HAS BEEN DONE}
```

App area can be `App`, `Editor`, `Sets`, `Relations`, `Auth`, `Settings`, `Analytics`, `Tech` (CI,
DI, scripting, etc.), etc.

Example: *Editor | Fix | Show meaningful message when failed to open file by an existing
application*

### Git branch naming

```
{TASK_ID or TASK_NUMBER}-ConciseDescription
```

### Flags

```
com.anytype.ci=true - for CI/CD pipeline
```

## Updating anytype-heart (basics)

Prerequisite: `brew install jq`

1. Run the following command in Terminal or Makefile:

    ```
    make update_mw
    ```

2. Make sure your proto files located in `protocol/main/proto/` compile.
3. Make sure to update `middlewareVersion` version in `libs.versions.toml`.

## Contribution
Thank you for your desire to develop Anytype together. 

Currently, we're not ready to accept PRs, but we will in the nearest future.

Follow us on [Github](https://github.com/anyproto) and join the [Contributors Community](https://github.com/orgs/anyproto/discussions).

---
Made by Any — a Swiss association 🇨🇭

Licensed under [Any Source Available License 1.0](./LICENSE.md).