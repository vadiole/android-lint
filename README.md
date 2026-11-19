# android-lint

Android Lint rules for bugs the built-in checks miss.

## Use

Clone this repo next to your app and include it as a composite build.

`settings.gradle.kts`:

```kotlin
includeBuild("../android-lint")
```

App module:

```kotlin
dependencies {
    lintChecks("vadiole:android-lint")
}
```

The rules then run with `./gradlew lint` and show up in Android Studio.
