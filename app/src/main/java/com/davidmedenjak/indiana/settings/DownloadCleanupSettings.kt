package com.davidmedenjak.indiana.settings

import java.time.Instant

data class DownloadCleanupSettings(
    val cleanupPeriod: CleanupPeriod = CleanupPeriod.THREE_DAYS,
    val lastCleanupTime: Instant? = null,
    val isEnabled: Boolean = true
)

enum class CleanupPeriod(val days: Int) {
    ONE_DAY(1),
    THREE_DAYS(3),
    SEVEN_DAYS(7),
    THIRTY_DAYS(30),
    NEVER(-1);

    companion object {
        fun fromDays(days: Int): CleanupPeriod {
            return entries.find { it.days == days } ?: THREE_DAYS
        }
    }
}
