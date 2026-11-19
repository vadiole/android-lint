## Android Lint

Custom Android Lint rules shared by every vadiole Android app.

- Apps consume this repo as a Gradle composite build, so a rule change reaches each app and worktree on its next `lintDebug`, with no release step:
  - `settings.gradle.kts`: `includeBuild(providers.gradleProperty("androidLint").getOrElse("../android-lint"))`
  - app module: `lintChecks("vadiole:android-lint")`
  - CI checks this repo out inside the app workspace and sets `ORG_GRADLE_PROJECT_androidLint` to that folder.
- The included build runs under each app's Gradle version, and the rules load into each app's lint version. Compile against the lint API of the oldest AGP in use across the apps (lint version = AGP version + 23), with Kotlin language and API level held at what that lint bundles.
- A rule that depends on the app's minSdk reports through a constraint such as `minSdkLessThan`: lint analyses modules before minSdk is known.
- Every rule has tests in `src/test` with the default test modes on; they rewrite the sample (parentheses, fully qualified names, partial analysis) and have caught real misses.
- New rule: a detector, its issues listed in `Registry`, tests, then `./gradlew test` and `lintDebug` in one app.
