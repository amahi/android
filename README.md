# Amahi Android App

This repository contains the source code for the Amahi Android app.

You will need JDK 1.7+ installed to work with it.
Gradle, Android SDK and project dependencies will be downloaded automatically.

## Building the app

1. Install the [LibVLC](https://github.com/amahi/libvlc-android).

    We have a pre-compiled
    [tarball of LibVLC for Android](https://dl.dropboxusercontent.com/u/364883/Amahi/maven-1.2.tar.bz2)
    if you rather not have to compile the whole library.
    It contains a `.m2` folder that should be placed in your `${HOME}` directory.

2. Set API information.

  ```
  $ vim api.properties
  ```
  ```
  url.amahi = URL
  url.proxy = URL
  client.id = ID
  client.secret = SECRET
  ```

3. Build the application.

  ```
  $ ./gradlew clean assembleDebug
  ```

## Generating the documentation

1. Install Android documentation.

  ```
  $ android update sdk --no-ui --force --all --filter doc-21
  ```

2. Generate the documentation.

  ```
  $ ./gradlew clean assembleDebug generateDebugJavadoc
  ```
