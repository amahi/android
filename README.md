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

This is something to keep private and you obtain it by asking in the [Amahi IRC channel](http://talk.amahi.org) or send a messag to `support at Amahi dot org`.

3. Build the application.

  ```
  $ ./gradlew clean assembleDebug

  ```

  or using the GUI.

4. Once you have built the application, you will be needing credentials to use the application. Go to Amahi website and create your account, once the account is activated, you will see the description of how to setup Amahi server. However, this is not strictly needed, as we have a set up a demo server called "Welcome to Amahi" which you should see even without your own server installed.


## Generating the documentation

1. Install Android documentation.

  ```
  $ android update sdk --no-ui --force --all --filter doc-21
  ```

2. Generate the documentation.

  ```
  $ ./gradlew clean assembleDebug generateDebugJavadoc
  ```
