# Amahi Anywhere

This repository contains the source code for the Amahi Android app.

## Building

You will need JDK 1.6, Android SDK 22 and Gradle 1.11 installed.

1. Install required Android components.

  ```
  $ android update sdk --no-ui --force --all --filter build-tools-19.0.3
  $ android update sdk --no-ui --force --all --filter android-19
  ```

2. Set API information.

  ```
  $ vim api.properties
  ```
  ```
  url.amahi = URL
  url.proxy = URL
  client.id = ID
  client.secret = SECRET
  client.token = TOKEN
  ```

3. Build application.

  ```
  $ gradle clean assembleDebug
  ```
