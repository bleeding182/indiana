package com.davidmedenjak.appupdate

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.google.android.play.core.appupdate.AppUpdateManagerFactory

/**
 * Wires and remembers an [AppUpdateState] backed by Google Play.
 *
 * It checks for an update on first composition, re-checks on `ON_RESUME`, auto-resumes an
 * interrupted immediate update, and cleans up its listener automatically. The module ships no UI;
 * build your own on top of the returned state, e.g.:
 *
 * ```
 * val state = rememberAppUpdateState()
 * if (state.hasNewVersionAvailableForMoreThan(5.days)) {
 *     showInfoPanelForUpdate(state)
 * }
 * ```
 */
@Composable
fun rememberAppUpdateState(): AppUpdateState {
    val context = LocalContext.current.applicationContext

    // Filled once the holder is created; the launcher callback reads it after composition.
    var holderRef by remember { mutableStateOf<AppUpdateStateHolder?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        holderRef?.onUpdateFlowResult(result.resultCode)
    }

    val holder = remember(launcher) {
        AppUpdateStateHolder(
            appUpdateManager = AppUpdateManagerFactory.create(context),
            launchIntentSender = { request -> launcher.launch(request) },
        ).also { holderRef = it }
    }

    DisposableEffect(holder) {
        holder.registerInstallListener()
        onDispose { holder.unregisterInstallListener() }
    }

    LaunchedEffect(holder) {
        holder.checkForUpdate()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        holder.checkForUpdate(resumeImmediateInProgress = true)
    }

    return holder
}
