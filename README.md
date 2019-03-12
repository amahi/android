# Amahi Android App     [![Build Status](https://travis-ci.org/amahi/android.svg?branch=master)](https://travis-ci.org/amahi/android)

This repository contains the source code for the Amahi Android app.

You will need JDK 1.7+ installed to work with it. Gradle, Android SDK, and project dependencies will be downloaded automatically.

## Building the app


1. Set API information.

  ```
  $ vim api.properties
  ```
  ```
  url.amahi = URL
  url.proxy = URL
  client.id = ID
  client.secret = SECRET
  chromecast.app.id = APP_ID
  ```

This is something to keep **private** and you obtain it by sending a message to `support at Amahi dot org`.


2. Build the application using a command line or using GUI.

  ```
  $ ./gradlew clean assembleDebug

  ```


3. Once you have built the application, you will be needing credentials to use the application. Go to Amahi website and create your account, once the account is activated, you will see the description on how to set up the Amahi server. However, this is not strictly needed, as we have a set up of a demo server called "Welcome to Amahi" which you should see even without your own server installed.

## Code Style Convention

* Set the Code Style Scheme to `Default` in Android Studio settings.
* Run `Reformat Code` on changed files before committing.
* Please check out the [Code Style for Contributors](https://source.android.com/source/code-style.html) section in AOSP and maintain as much consistency with them as possible.

## Generating the documentation

1. Install Android documentation.

  ```
  $ android update sdk --no-ui --force --all --filter doc-21
  ```

2. Generate the documentation.

  ```
  $ ./gradlew clean assembleDebug generateDebugJavadoc
  ```

## Debugging

* For debugging the APIs and logging requests and responses you can either use `Logcat` on Android Studio or enable `Chuck Interceptor` (UI based) from the steps given [here](DEBUG.md#enabling-chuck-interceptor).

* To debug with some special purpose server, you can find steps [here](DEBUG.md#using-a-custom-server).
