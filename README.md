# Amahi Android App

This repository contains the source code for the Amahi Android app.

## Building

You will need JDK 1.6, Android SDK 22 and Gradle 1.12 installed.

1. Install Android components.

  ```
  $ android update sdk --no-ui --force --all --filter build-tools-19.1
  $ android update sdk --no-ui --force --all --filter android-19
  $ android update sdk --no-ui --force --all --filter extra-android-m2repository
  ```

2. Install VLC dependency using [this project](https://github.com/ming13/libvlc-android).

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

4. Build application.

  ```
  $ gradle clean assembleDebug
  ```
