package com.davidmedenjak.appupdate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.time.Duration

/**
 * A fully-controllable [AppUpdateState] for `@Preview`s and tests. Every property is a public,
 * mutable Compose state so any combination (available, downloading, downloaded, stale, failed) can
 * be fabricated without Google Play. Methods are no-ops.
 */
class FakeAppUpdateState(
    availability: UpdateAvailability = UpdateAvailability.Unknown,
    installStatus: InstallStatus = InstallStatus.Unknown,
    availableVersionCode: Int? = null,
    stalenessDays: Int? = null,
    updatePriority: Int = 0,
    downloadProgress: Float? = null,
    isFlexibleAllowed: Boolean = false,
    isImmediateAllowed: Boolean = false,
    lastError: Throwable? = null,
) : AppUpdateState {
    override var availability: UpdateAvailability by mutableStateOf(availability)
    override var installStatus: InstallStatus by mutableStateOf(installStatus)
    override var availableVersionCode: Int? by mutableStateOf(availableVersionCode)
    override var stalenessDays: Int? by mutableStateOf(stalenessDays)
    override var updatePriority: Int by mutableStateOf(updatePriority)
    override var downloadProgress: Float? by mutableStateOf(downloadProgress)
    override var isFlexibleAllowed: Boolean by mutableStateOf(isFlexibleAllowed)
    override var isImmediateAllowed: Boolean by mutableStateOf(isImmediateAllowed)
    override var lastError: Throwable? by mutableStateOf(lastError)

    override fun hasNewVersionAvailableForMoreThan(duration: Duration): Boolean {
        val staleness = stalenessDays ?: return false
        return staleness >= duration.inWholeDays
    }

    override fun startFlexibleUpdate() = Unit
    override fun startImmediateUpdate() = Unit
    override fun completeFlexibleUpdate() = Unit
    override fun refresh() = Unit
}
