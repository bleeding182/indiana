package com.davidmedenjak.appupdate

import androidx.compose.runtime.Stable
import kotlin.time.Duration

/**
 * Observable snapshot of the Google Play in-app-update status plus the actions that drive it.
 *
 * Obtain an instance via [rememberAppUpdateState]. All properties are Compose state, so reading
 * them in composition triggers recomposition when the update status changes. No `com.google.android.play`
 * type is ever exposed.
 */
@Stable
interface AppUpdateState {
    val availability: UpdateAvailability
    val installStatus: InstallStatus
    val availableVersionCode: Int?
    val stalenessDays: Int?
    val updatePriority: Int
    val downloadProgress: Float?
    val isFlexibleAllowed: Boolean
    val isImmediateAllowed: Boolean
    val lastError: Throwable?

    /**
     * `true` when an update has been available for at least [duration] (day granularity, as that is
     * all Google Play reports). Returns `false` when staleness is unknown, so UI defaults to
     * non-nagging behavior.
     */
    fun hasNewVersionAvailableForMoreThan(duration: Duration): Boolean

    /** Start a background (flexible) update; the app keeps running while it downloads. */
    fun startFlexibleUpdate()

    /** Start a blocking, full-screen (immediate) update driven by Google Play. */
    fun startImmediateUpdate()

    /** Install and restart after a flexible update finished downloading. */
    fun completeFlexibleUpdate()

    /** Force a re-check of the update status. */
    fun refresh()
}
