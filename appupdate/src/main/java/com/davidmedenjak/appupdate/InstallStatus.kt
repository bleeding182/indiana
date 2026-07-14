package com.davidmedenjak.appupdate

/** Progress of an in-flight update install, mirrored from Google Play. */
enum class InstallStatus {
    Unknown,
    Pending,
    Downloading,
    Downloaded,
    Installing,
    Installed,
    Failed,
    Canceled,
}
