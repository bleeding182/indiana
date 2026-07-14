# :appupdate

A dependency-injection-agnostic Compose module that wraps the Google Play in-app-update flow and
publishes update status as observable Compose `State`. It ships **no UI** — you build your own on
top of the state it exposes.

## Usage

```kotlin
val state = rememberAppUpdateState()

// Gate your own UI on the published state:
if (state.hasNewVersionAvailableForMoreThan(5.days)) {
    showInfoPanelForUpdate(state)
}

// Trigger a flow when the user opts in:
when (state.availability) {
    UpdateAvailability.Available -> Button(onClick = { state.startFlexibleUpdate() }) { /* ... */ }
    else -> Unit
}

// Flexible updates never auto-install; prompt, then complete on your terms:
if (state.installStatus == InstallStatus.Downloaded) {
    RestartBanner(onRestart = { state.completeFlexibleUpdate() })
}
```

`rememberAppUpdateState()` checks for an update on first composition, re-checks on `ON_RESUME`,
auto-resumes an interrupted immediate update, and cleans up its listener automatically. No `Activity`
is retained.

### Reacting to the dialog outcome

`installStatus` reflects the download/install lifecycle, but a dialog dismissal is a one-shot event.
For transient reactions (e.g. a snackbar) pass an `onUpdateFlowResult` callback — it fires once per
flow with `Accepted` / `Canceled` / `Failed`:

```kotlin
val state = rememberAppUpdateState { result ->
    when (result) {
        UpdateFlowResult.Canceled -> snackbar("Update canceled")
        is UpdateFlowResult.Failed -> snackbar("Update failed")
        UpdateFlowResult.Accepted -> Unit
    }
}
```

## State

`AppUpdateState` exposes (all Compose-`State`-backed):

- `availability: UpdateAvailability` — `Unknown` / `NotAvailable` / `Available` / `InProgress`
- `installStatus: InstallStatus` — `Unknown` / `Pending` / `Downloading` / `Downloaded` / `Installing` / `Installed` / `Failed` / `Canceled`
- `availableVersionCode: Int?`, `stalenessDays: Int?`, `updatePriority: Int`
- `downloadProgress: Float?` (0f–1f while downloading)
- `isFlexibleAllowed` / `isImmediateAllowed`, `lastError: Throwable?`

Actions: `startFlexibleUpdate()`, `startImmediateUpdate()`, `completeFlexibleUpdate()`, `refresh()`,
and `hasNewVersionAvailableForMoreThan(duration)`.

> **Note:** Play reports staleness only in whole days (`clientVersionStalenessDays`, often null), so
> `hasNewVersionAvailableForMoreThan` is day-granular and returns `false` when staleness is unknown.

## Previews & tests

`FakeAppUpdateState` implements `AppUpdateState` with settable properties so you can render any state
in `@Preview` or unit tests without Google Play:

```kotlin
@Preview
@Composable
private fun UpdateBannerPreview() {
    UpdateBanner(FakeAppUpdateState(availability = UpdateAvailability.Available, stalenessDays = 10))
}
```
