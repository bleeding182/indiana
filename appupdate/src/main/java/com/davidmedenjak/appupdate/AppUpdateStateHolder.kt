package com.davidmedenjak.appupdate

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityOptionsCompat
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import java.util.concurrent.Executor
import kotlin.time.Duration
import com.google.android.play.core.install.model.InstallStatus as PlayInstallStatus
import com.google.android.play.core.install.model.UpdateAvailability as PlayUpdateAvailability

/**
 * The single seam of the module: all logic lives here, decoupled from Compose.
 *
 * Production wires the real [AppUpdateManager] and a Compose-backed launcher; tests supply Play's
 * `FakeAppUpdateManager` and a capturing [launchIntentSender].
 */
internal class AppUpdateStateHolder(
    private val appUpdateManager: AppUpdateManager,
    private val launchIntentSender: (IntentSenderRequest) -> Unit,
) : AppUpdateState {

    override var availability: UpdateAvailability by mutableStateOf(UpdateAvailability.Unknown)
        private set
    override var installStatus: InstallStatus by mutableStateOf(InstallStatus.Unknown)
        private set
    override var availableVersionCode: Int? by mutableStateOf(null)
        private set
    override var stalenessDays: Int? by mutableStateOf(null)
        private set
    override var updatePriority: Int by mutableStateOf(0)
        private set
    override var downloadProgress: Float? by mutableStateOf(null)
        private set
    override var isFlexibleAllowed: Boolean by mutableStateOf(false)
        private set
    override var isImmediateAllowed: Boolean by mutableStateOf(false)
        private set
    override var lastError: Throwable? by mutableStateOf(null)
        private set

    private var currentInfo: AppUpdateInfo? = null

    // Runs Play Task callbacks synchronously on the completing thread, avoiding a main Looper
    // dependency (Compose snapshot state is safe to write from any thread).
    private val directExecutor = Executor { it.run() }

    private val installListener = InstallStateUpdatedListener { state -> onInstallState(state) }

    // Bridges the module's launch lambda to Play's launcher-based update flow so no Activity is held.
    private val resultLauncher = object : ActivityResultLauncher<IntentSenderRequest>() {
        override fun launch(input: IntentSenderRequest, options: ActivityOptionsCompat?) {
            launchIntentSender(input)
        }

        override fun unregister() = Unit

        override val contract: ActivityResultContract<IntentSenderRequest, *> =
            ActivityResultContracts.StartIntentSenderForResult()
    }

    fun registerInstallListener() = appUpdateManager.registerListener(installListener)

    fun unregisterInstallListener() = appUpdateManager.unregisterListener(installListener)

    override fun refresh() = checkForUpdate()

    fun checkForUpdate(resumeImmediateInProgress: Boolean = false) {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener(directExecutor) { info ->
                applyInfo(info)
                if (resumeImmediateInProgress &&
                    info.updateAvailability() == PlayUpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    startImmediateUpdate()
                }
            }
            .addOnFailureListener(directExecutor) { error ->
                availability = UpdateAvailability.Unknown
                lastError = error
            }
    }

    override fun startFlexibleUpdate() = startUpdate(AppUpdateType.FLEXIBLE)

    override fun startImmediateUpdate() = startUpdate(AppUpdateType.IMMEDIATE)

    private fun startUpdate(@AppUpdateType type: Int) {
        val info = currentInfo ?: return
        runCatching {
            appUpdateManager.startUpdateFlowForResult(
                info,
                resultLauncher,
                AppUpdateOptions.newBuilder(type).build(),
            )
        }.onFailure {
            installStatus = InstallStatus.Failed
            lastError = it
        }
    }

    override fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }

    /** Maps the result of the Play update dialog launched through [resultLauncher]. */
    fun onUpdateFlowResult(resultCode: Int) {
        when (resultCode) {
            Activity.RESULT_OK -> Unit
            Activity.RESULT_CANCELED -> installStatus = InstallStatus.Canceled
            else -> {
                installStatus = InstallStatus.Failed
                lastError = RuntimeException("Update flow failed with resultCode $resultCode")
            }
        }
    }

    override fun hasNewVersionAvailableForMoreThan(duration: Duration): Boolean {
        val staleness = stalenessDays ?: return false
        return staleness >= duration.inWholeDays
    }

    private fun applyInfo(info: AppUpdateInfo) {
        currentInfo = info
        availability = when (info.updateAvailability()) {
            PlayUpdateAvailability.UPDATE_AVAILABLE -> UpdateAvailability.Available
            PlayUpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> UpdateAvailability.InProgress
            PlayUpdateAvailability.UPDATE_NOT_AVAILABLE -> UpdateAvailability.NotAvailable
            else -> UpdateAvailability.Unknown
        }
        availableVersionCode = if (availability == UpdateAvailability.Available ||
            availability == UpdateAvailability.InProgress
        ) {
            info.availableVersionCode()
        } else {
            null
        }
        stalenessDays = info.clientVersionStalenessDays()
        updatePriority = info.updatePriority()
        isFlexibleAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
        isImmediateAllowed = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)

        // A routine recheck (e.g. on ON_RESUME, which fires right after the update dialog's result)
        // must not erase a user-visible Canceled/Failed flow result: Play reports UNKNOWN for a
        // dismissed dialog, so only apply its install status when it has a real one, and keep the
        // flow error otherwise.
        val playStatus = info.installStatus()
        val hasFlowResult =
            installStatus == InstallStatus.Canceled || installStatus == InstallStatus.Failed
        if (playStatus != PlayInstallStatus.UNKNOWN || !hasFlowResult) {
            lastError = null
            applyInstallStatus(playStatus, info.bytesDownloaded(), info.totalBytesToDownload())
        }
    }

    private fun onInstallState(state: InstallState) {
        applyInstallStatus(state.installStatus(), state.bytesDownloaded(), state.totalBytesToDownload())
        if (state.installStatus() == PlayInstallStatus.FAILED) {
            lastError = RuntimeException("Install failed with error code ${state.installErrorCode()}")
        }
    }

    private fun applyInstallStatus(
        @PlayInstallStatus playStatus: Int,
        bytesDownloaded: Long,
        totalBytesToDownload: Long,
    ) {
        installStatus = when (playStatus) {
            PlayInstallStatus.PENDING -> InstallStatus.Pending
            PlayInstallStatus.DOWNLOADING -> InstallStatus.Downloading
            PlayInstallStatus.DOWNLOADED -> InstallStatus.Downloaded
            PlayInstallStatus.INSTALLING -> InstallStatus.Installing
            PlayInstallStatus.INSTALLED -> InstallStatus.Installed
            PlayInstallStatus.FAILED -> InstallStatus.Failed
            PlayInstallStatus.CANCELED -> InstallStatus.Canceled
            else -> InstallStatus.Unknown
        }
        downloadProgress = if (playStatus == PlayInstallStatus.DOWNLOADING && totalBytesToDownload > 0) {
            (bytesDownloaded.toFloat() / totalBytesToDownload.toFloat()).coerceIn(0f, 1f)
        } else {
            null
        }
    }
}
