# Amahi Android App

This repository contains the source code for the Amahi Android app.

You will need JDK 1.7 and Android SDK 21 installed to work with it.

## Building the app

1. Install Android components.

  ```
  $ android update sdk --no-ui --force --all --filter build-tools-21.0.1
  $ android update sdk --no-ui --force --all --filter android-21
  $ android update sdk --no-ui --force --all --filter extra-android-m2repository
  ```

2. Install the [LibVLC library](https://github.com/amahi/libvlc-android).

    We have a pre-compiled [tarball of LibVLC for Android](https://dl.dropboxusercontent.com/u/364883/Amahi/maven-1.2.tar.bz2) if you rather not have to compile the whole library.
    It contains a .m2 folder that should be placed in your $HOME directory.

3. Set API information.

  ```
  $ vim api.properties
  ```
  ```
  url.amahi = URL
  url.proxy = URL
  client.id = ID
  client.secret = SECRET
  ```

4. Build the application.

  ```
  $ ./gradlew clean assembleDebug
  ```

## Generating the documentation

1. Install Android documentation.

  ```
  $ android update sdk --no-ui --force --all --filter doc-L
  ```

2. Generate the documentation.

  ```
  $ ./gradlew clean generateDebugJavadoc
  ```
