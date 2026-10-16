# android-app

Kotlin Android application for FedRoad-Net.

**Status:** Phase 2 skeleton. Builds a minimal app that shows a status string. Sensor
capture/windowing (Phase 3), on-device inference (Phase 5), and the consent UI are added
later.

## Toolchain (verified)

- Gradle 8.13 (wrapper), Android Gradle Plugin 8.13.2, Kotlin 2.2.21
- `compileSdk`/`targetSdk` = 36, `minSdk` = 24, JDK 17 bytecode (built with JDK 21)
- Gradle wrapper scripts/`gradle-wrapper.jar` are the standard Gradle wrapper, copied
  from `flwrlabs/flower` `examples/android-kotlin/client` (Apache-2.0).

## Android SDK

Point Gradle at your SDK via either:

- `android-app/local.properties` (gitignored):
  ```
  sdk.dir=/absolute/path/to/Android/Sdk
  ```
- or the `ANDROID_HOME` / `ANDROID_SDK_ROOT` environment variables.

## Build / test

```sh
make android-build     # from repo root → ./gradlew assembleDebug
cd android-app && ./gradlew test    # JVM unit tests
```

Build outputs (`app/build/`, `.gradle/`) and `local.properties` are gitignored.
