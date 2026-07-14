package com.davidmedenjak.appupdate

/** Whether an update is available, mirrored from Google Play without leaking Play types. */
enum class UpdateAvailability {
    Unknown,
    NotAvailable,
    Available,
    InProgress,
}
