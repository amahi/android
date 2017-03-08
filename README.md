# Amahi Android App

This repository contains the source code for the Amahi Android app.

You will need JDK 1.7+ installed to work with it.
Gradle, Android SDK and project dependencies will be downloaded automatically.

## Building the app

1. Install the [LibVLC](https://github.com/amahi/libvlc-android).

    We have a pre-compiled
    [tarball of LibVLC for Android](https://dl.dropboxusercontent.com/u/364883/Amahi/maven-2.0.tar.gz)
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
4. Once you have build the application, you will be needing credentials to access the application. Go to Amahi website and create your account, once the account is activated, you will see the description of how to setup Amahi server. The process is explained for Fedora 23/25 which are currently supported.

5. If you just want to getting started then initially you can use the demo files provided by community. To access that you have to ask for api related details which due to privacy reasons have been kept private. Ask any of the amahi community member for the same. Make sure you don't share them without permission of the community.

## Generating the documentation

1. Install Android documentation.

  ```
  $ android update sdk --no-ui --force --all --filter doc-21
  ```

2. Generate the documentation.

  ```
  $ ./gradlew clean assembleDebug generateDebugJavadoc
  ```
