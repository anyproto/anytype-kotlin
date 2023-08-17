# How to setup environment for building middleware
## MacOS

- Open a terminal.
- Remove all other golang installations (`which go` should report that nothing found)
- Make a dir for golang `mkdir -p $HOME/golang`
- Update brew by `brew update`
- Install from brew only a golang version mentioned here: [here](https://github.com/anyproto/anytype-heart#build-from-source)
For example: `brew install go@1.xx`
- Remember a golang install path (`/<path-to-golang>/go@1.xx/`) reported by brew 

- If you have `~/.zprofile` file then add these lines here, else add these lines to `~/.zshrc`

```
export GOPATH=$HOME/golang
export GOROOT=/<path-to-go>/go@1.xx/libexec 
export PATH=$PATH:$GOPATH/bin
export PATH=$PATH:$GOROOT/bin
```
- After restart of the terminal you will achieve all the variables set, 
or you can simply load them in the current terminal window by `source ~/.zprofile` or `source ~/.zshrc`
- Make sure you have set `ANDROID_HOME` variable in the same file
- Install `NDK 23.2.8568313` via an Android SDK Package Manager

Now you can build the middleware library for android.

# How to setup custom middleware (go) library for Anytype Android project

1. Put your custom 'libs.aar' to the `/libs` directory.
2. Update proto files in `protocol` module.
3. Specify `version` for your custom library in `/libs/build.gradle` file. To avoid conflicts, this version should be higher than the latest release version in `anytype-heart' repository.
4. Run the following Gradle command: `./gradlew libs:publishToMavenLocal`.
5. Specify `version` from step 3 in `libs.version` file as `middlewareVersion`.
6. Rebuild project.