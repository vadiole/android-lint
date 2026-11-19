## Android Lint

Custom Android Lint rules for vadiole's Android apps. Public repo.

- Apps include this repo as a composite build, so a change reaches every app and worktree on its next lint run. There are no releases: keep `master` working.
- Compile against the lint API of the oldest AGP any app uses (lint version = AGP version + 23), with the Kotlin language and API version that lint bundles.
- Each rule has tests that pass in every default test mode.
- A rule is documented by its issue explanation; the README covers use only.
