# Spec: Reusable In-App Update Compose Module (`:appupdate`)

## Problem Statement

As a developer, I have a working Google Play in-app-update flow inside the Indiana app, but it is
welded into the app: the logic lives in a Hilt `@Singleton` (`InAppUpdateManager`), it is reached
through a Hilt `EntryPoint` from a specific screen route, and the UI decisions (a staleness dot, an
"Update app" overflow item) are hard-coded into `ProjectsScreen`. I want to reuse this update
capability in a *different* app, where I will build my own UI and may not use Hilt at all.

The current code also carries defects that make it unsafe to lift as-is:

- It stores an `Activity` in a process-wide `@Singleton` (leak) and never clears it.
- It implements `DefaultLifecycleObserver` but is never registered, so its `InstallStateUpdatedListener` is never unregistered (leak / dead code).
- On `DOWNLOADED` it immediately calls `completeUpdate()`, restarting the app without user consent.
- "Available for more than N days" is a plain function read during composition, hard-coded to 3 days, non-reactive.
- The activity-result callback for the Play dialog is empty, so user-cancel and failure are invisible.
- Only FLEXIBLE updates are supported; no IMMEDIATE flow, no download progress, no resume of an interrupted update.

## Solution

Extract the update capability into a standalone, dependency-injection-agnostic Compose library
module, `:appupdate`, that owns the entire data layer (talking to Google Play) and publishes update
status as observable Compose `State`. The consumer gets a single entry point,
`rememberAppUpdateState()`, returning a stable `AppUpdateState` object whose properties recompose on
change and whose methods trigger the Play update flows. The consumer builds 100% of their own UI on
top of these state properties — for example:

```kotlin
val state = rememberAppUpdateState()
if (state.hasNewVersionAvailableForMoreThan(5.days)) {
    showInfoPanelForUpdate(state)
}
```

The module exposes only module-owned domain types (no `com.google.android.play` types leak into
consumer code), ships a fake implementation for `@Preview`/tests, and Indiana itself is migrated to
consume it (proving the API and deleting the buggy code).

## User Stories

1. As a developer, I want a standalone `:appupdate` Gradle module, so that I can copy/depend on it from another app without pulling in Indiana-specific code.
2. As a developer, I want the module to have no dependency on Hilt or any DI framework, so that it works in an app with a different (or no) DI setup.
3. As a developer, I want the module to depend only on the Compose runtime and the Google Play app-update libraries, so that its footprint is small and predictable.
4. As a developer, I want a single `rememberAppUpdateState()` composable entry point, so that I don't have to wire managers, listeners, or launchers myself.
5. As a developer, I want `AppUpdateState` to be `@Stable`/observable, so that reading its properties in composition triggers recomposition when the update status changes.
6. As a developer, I want update availability exposed as a module-owned enum (`Unknown`, `NotAvailable`, `Available`, `InProgress`), so that my UI code never imports Play types.
7. As a developer, I want install status exposed as a module-owned enum (`Unknown`, `Pending`, `Downloading`, `Downloaded`, `Installing`, `Installed`, `Failed`, `Canceled`), so that I can drive UI (e.g. show a "restart to install" affordance) from a clean model.
8. As a developer, I want the available version code exposed, so that I can display or log which version the update targets.
9. As a developer, I want the Play update priority (0–5) exposed, so that I can decide between offering a flexible or immediate update.
10. As a developer, I want the staleness in days exposed (nullable), so that I can reason about how long an update has been available.
11. As a developer, I want a `hasNewVersionAvailableForMoreThan(duration)` helper, so that I can gate UI (e.g. show a nudge after 5 days) with a single readable call.
12. As a developer, I want that helper to return `false` when staleness is unknown/null, so that my UI defaults to non-nagging behavior when Play gives no signal.
13. As a developer, I want the module to check for updates automatically on first composition, so that state is populated without my intervention.
14. As a developer, I want the module to re-check on `ON_RESUME`, so that returning from the Play Store page refreshes the state.
15. As a developer, I want a `refresh()` method, so that I can force a re-check on demand (e.g. from a pull-to-refresh).
16. As a developer, I want a `startFlexibleUpdate()` method, so that the app keeps running while the update downloads in the background.
17. As a developer, I want a `startImmediateUpdate()` method, so that I can force a blocking, full-screen Play-driven update when needed.
18. As a developer, I want download progress exposed as a `Float?` (0f–1f) during downloading, so that I can render my own progress indicator.
19. As a developer, I want the module to NOT auto-install a flexible update, so that my UI decides when the disruptive restart happens.
20. As a developer, I want a `completeFlexibleUpdate()` method, so that my UI can trigger the install/restart after prompting the user.
21. As a developer, I want the module to auto-resume an interrupted immediate update on resume, so that I comply with Google Play's immediate-update requirements without extra code.
22. As a developer, I want user-cancellation of the Play dialog reflected in `installStatus` (`Canceled`), so that I can react (e.g. re-offer later) without wiring an activity result myself.
23. As a developer, I want failures reflected in `installStatus` (`Failed`) and a nullable `lastError`, so that I can log or message the problem.
24. As a developer, I want errors during the availability check folded into state (`availability = Unknown`, `lastError` set), so that a failed check never crashes the app.
25. As a developer, I want the module to degrade gracefully on a sideloaded/debug build with no Play, so that development builds show "no update" instead of crashing.
26. As a developer, I want `isFlexibleAllowed` / `isImmediateAllowed` booleans, so that I only surface actions Play actually permits for the current update.
27. As a developer, I want `AppUpdateState` to be an interface, so that I can substitute a fake in previews and tests.
28. As a developer, I want a constructable `FakeAppUpdateState`, so that I can render every UI state (available, downloading, downloaded, stale-10-days, failed) in `@Preview` and in tests without Google Play.
29. As a developer, I want no UI shipped in the module, so that nothing conflicts with my own design system.
30. As a developer, I want the module free of any `:theme` dependency, so that it drops into an app that has no Indiana theme.
31. As an Indiana maintainer, I want `ProjectsRoute` migrated to `rememberAppUpdateState()`, so that the app exercises the new module as a real consumer.
32. As an Indiana user, I want to keep seeing the staleness dot on the overflow menu, so that the migration is behavior-preserving.
33. As an Indiana user, I want to keep seeing the "Update app" menu item when an update is available or downloaded, so that I can still trigger the update.
34. As an Indiana user, when a flexible update has finished downloading, I want a restart affordance rather than a surprise restart, so that I control when the app restarts.
35. As an Indiana maintainer, I want `InAppUpdateManager` and `UpdateManagerEntryPoint` deleted after migration, so that the buggy leaking code is gone.
36. As a developer, I want a short README/sample showing the `hasNewVersionAvailableForMoreThan(5.days)` pattern, so that I can integrate the module into the next app quickly.

## Implementation Decisions

**New module**
- Add a new Android library module `:appupdate`, registered in `settings.gradle.kts`, applying the `indiana.convention` plugin (shared SDK/Java/Kotlin config) and enabling Compose.
- Namespace/package: `com.davidmedenjak.appupdate`.
- Dependencies limited to: Compose runtime, `androidx.lifecycle` (compose + runtime), `androidx.activity.compose` (for the intent-sender launcher), and `play-app-update` / `play-app-update-ktx`. No Hilt, no `:theme`, no Material.

**Public API** (module-owned types only; no Play types in signatures)
```kotlin
@Composable
fun rememberAppUpdateState(): AppUpdateState

@Stable
interface AppUpdateState {
    val availability: UpdateAvailability      // Unknown, NotAvailable, Available, InProgress
    val installStatus: InstallStatus          // Unknown, Pending, Downloading, Downloaded,
                                              // Installing, Installed, Failed, Canceled
    val availableVersionCode: Int?
    val stalenessDays: Int?
    val updatePriority: Int
    val downloadProgress: Float?              // 0f..1f while Downloading
    val isFlexibleAllowed: Boolean
    val isImmediateAllowed: Boolean
    val lastError: Throwable?

    fun hasNewVersionAvailableForMoreThan(duration: Duration): Boolean  // day granularity; false if null
    fun startFlexibleUpdate()
    fun startImmediateUpdate()
    fun completeFlexibleUpdate()
    fun refresh()
}
```

**Architecture / seam**
- All logic lives in a plain, non-composable internal state holder class. Its dependencies are Play's `AppUpdateManager` (constructor-injected) and a "launch intent sender" lambda. This is the single seam of the module — production supplies the real `AppUpdateManagerFactory.create(...)` and a Compose-backed launcher; tests supply Play's `FakeAppUpdateManager` and a capturing lambda.
- `rememberAppUpdateState()` is a thin wiring shell: it builds the `AppUpdateManager` from `LocalContext`, obtains an intent-sender launcher via `rememberLauncherForActivityResult(StartIntentSenderForResult())` (so **no `Activity` is ever stored** — fixes the leak), remembers the holder, and registers/unregisters the `InstallStateUpdatedListener` in a `DisposableEffect` (fixes the listener leak).
- Automatic behavior: check on first composition; re-check on `ON_RESUME`; on resume, if an immediate update is in progress (`DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS`), auto-resume it. Flexible completion is never automatic.
- The launcher result maps `RESULT_CANCELED` → `installStatus = Canceled` and other non-OK results → `installStatus = Failed` with `lastError` set.
- Staleness helper: `hasNewVersionAvailableForMoreThan(duration)` compares `duration.inWholeDays` against Play's `clientVersionStalenessDays()`, returning `false` when the value is null. Day granularity is an accepted Play constraint (sub-day durations cannot be honored).

**Testability**
- `AppUpdateState` is an interface; the real implementation is internal. A public `FakeAppUpdateState` (a plain holder whose fields are mutable/State-backed) lets consumers fabricate any combination of properties for previews and tests.

**Indiana migration**
- `ProjectsRoute` calls `rememberAppUpdateState()` instead of resolving the Hilt singleton via `UpdateManagerEntryPoint`.
- Behavior preserved in `ProjectsScreen`: staleness dot driven by `state.hasNewVersionAvailableForMoreThan(3.days)`; "Update app" item shown when `availability == Available` or `installStatus == Downloaded`; the item's `onClick` calls `startFlexibleUpdate()`.
- Replace the auto-complete-on-downloaded behavior with a restart affordance that calls `completeFlexibleUpdate()`.
- Delete `InAppUpdateManager.kt` and the `UpdateManagerEntryPoint` interface. The `play-app-update` dependencies move from `:app` to `:appupdate` (`:app` gets them transitively via the module).

## Testing Decisions

- **What makes a good test here:** assert only externally observable behavior of the module — the values published on `AppUpdateState` and the side effects invoked on the Play boundary (which flow was launched, whether `completeUpdate` was called). Do not assert on private fields, listener registration mechanics, or Compose internals.
- **Module under test:** `:appupdate`, exercised through the internal state holder at its single seam.
- **Approach:** drive the holder with Google Play's `FakeAppUpdateManager` (from the `app-update` library's testing package) to simulate: no update, update available, user accepts, downloading with progress, downloaded, user cancels, install failure, and immediate-update-in-progress on resume. Assert the resulting `availability` / `installStatus` / `downloadProgress` / `lastError` and that `hasNewVersionAvailableForMoreThan` maps staleness correctly (including null → false, and boundary at exactly N days). Capture the launch lambda to assert flexible vs immediate flows are triggered as expected.
- **Prior art:** the only existing unit tests are in `:lint` (`Material3ImportAliasDetectorTest`), using plain JUnit4 (`junit = 4.13.2`). Follow that JUnit4 style. A JVM-only test source set is preferred (no instrumentation) so the state holder is tested without a device; the composable wiring shell is deliberately thin and left to manual/preview verification.

## Out of Scope

- Any consumer UI (banners, dialogs, snackbars, info panels) — the module ships no UI.
- Persisting a locally-tracked "first seen" timestamp for finer-than-day staleness; staleness comes solely from Play.
- Non–Google-Play update mechanisms (e.g. custom APK download/install, the Bitrise artifact install path already in Indiana).
- Publishing `:appupdate` to a Maven repository; it is consumed as a source module for now (copy or project dependency).
- Localized user-facing strings inside the module (the consumer owns all copy).
- Instrumented/UI tests of `rememberAppUpdateState()` wiring on a device.

## Further Notes

- Google Play only surfaces staleness in whole days via `clientVersionStalenessDays()` and it is frequently null; `hasNewVersionAvailableForMoreThan` is documented to reflect this.
- `updatePriority` requires setting update priority in the Play Console at release time to be meaningful; it defaults to 0 otherwise. It is exposed because it is free and is the natural input for choosing immediate vs flexible.
- Auto-resuming immediate updates is effectively mandated by Google's in-app-update guidance and is therefore handled by the module rather than left to the consumer.
- Package name and the inclusion of `updatePriority` were decided by the author rather than requested by the user; easy to change if the next app prefers a neutral package namespace.
