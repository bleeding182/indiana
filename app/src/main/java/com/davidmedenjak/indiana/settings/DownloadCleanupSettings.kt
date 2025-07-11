package com.davidmedenjak.indiana.settings

import java.time.Instant

data class DownloadCleanupSettings(
    val cleanupPeriod: CleanupPeriod = CleanupPeriod.THREE_DAYS,
    val lastCleanupTime: Instant? = null,
    val isEnabled: Boolean = true
)

enum class CleanupPeriod(val days: Int, val displayName: String) {
    ONE_DAY(1, "1 day"),
    THREE_DAYS(3, "3 days"),
    SEVEN_DAYS(7, "7 days"),
    THIRTY_DAYS(30, "30 days"),
    NEVER(-1, "Never");

    companion object {
        fun fromDays(days: Int): CleanupPeriod {
            return values().find { it.days == days } ?: THREE_DAYS
        }
    }
}