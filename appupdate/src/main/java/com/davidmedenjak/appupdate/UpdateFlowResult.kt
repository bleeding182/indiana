package com.davidmedenjak.appupdate

/**
 * One-shot outcome of a Play update dialog launched via [AppUpdateState.startFlexibleUpdate] or
 * [AppUpdateState.startImmediateUpdate]. Delivered to the `onUpdateFlowResult` callback of
 * [rememberAppUpdateState] exactly once per flow — use it for transient reactions (e.g. a snackbar)
 * that the recompose-friendly [AppUpdateState.installStatus] can't model cleanly.
 */
sealed interface UpdateFlowResult {
    /** The user accepted the update; for flexible updates the download is starting. */
    object Accepted : UpdateFlowResult

    /** The user dismissed the update dialog. */
    object Canceled : UpdateFlowResult

    /** The flow failed; [resultCode] is the raw activity result code. */
    data class Failed(val resultCode: Int) : UpdateFlowResult
}
