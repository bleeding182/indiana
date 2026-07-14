package com.davidmedenjak.appupdate

import android.app.Activity
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.InstallErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.time.Duration.Companion.days

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppUpdateStateHolderTest {

    private lateinit var fake: FakeAppUpdateManager
    private val launched = mutableListOf<IntentSenderRequest>()
    private lateinit var holder: AppUpdateStateHolder

    @Before
    fun setUp() {
        fake = FakeAppUpdateManager(RuntimeEnvironment.getApplication())
        holder = AppUpdateStateHolder(fake) { launched += it }
        holder.registerInstallListener()
    }

    @Test
    fun noUpdate() {
        fake.setUpdateNotAvailable()
        holder.checkForUpdate()

        assertEquals(UpdateAvailability.NotAvailable, holder.availability)
        assertNull(holder.availableVersionCode)
        assertNull(holder.lastError)
    }

    @Test
    fun updateAvailable() {
        fake.setUpdateAvailable(42)
        holder.checkForUpdate()

        assertEquals(UpdateAvailability.Available, holder.availability)
        assertEquals(42, holder.availableVersionCode)
        assertTrue(holder.isFlexibleAllowed)
        assertTrue(holder.isImmediateAllowed)
    }

    @Test
    fun downloadingReportsProgress() {
        fake.setUpdateAvailable(7)
        holder.checkForUpdate()
        holder.startFlexibleUpdate()
        fake.userAcceptsUpdate()
        fake.downloadStarts()

        assertEquals(InstallStatus.Downloading, holder.installStatus)

        fake.setTotalBytesToDownload(100)
        fake.setBytesDownloaded(40)

        assertEquals(0.4f, holder.downloadProgress)
    }

    @Test
    fun downloadedClearsProgress() {
        fake.setUpdateAvailable(7)
        holder.checkForUpdate()
        holder.startFlexibleUpdate()
        fake.userAcceptsUpdate()
        fake.downloadStarts()
        fake.downloadCompletes()

        assertEquals(InstallStatus.Downloaded, holder.installStatus)
        assertNull(holder.downloadProgress)
    }

    @Test
    fun userCancelMapsToCanceled() {
        holder.onUpdateFlowResult(Activity.RESULT_CANCELED)

        assertEquals(InstallStatus.Canceled, holder.installStatus)
    }

    @Test
    fun failedResultMapsToFailed() {
        holder.onUpdateFlowResult(Activity.RESULT_FIRST_USER)

        assertEquals(InstallStatus.Failed, holder.installStatus)
        assertNotNull(holder.lastError)
    }

    @Test
    fun checkErrorFoldsIntoState() {
        fake.setUpdateAvailable(1)
        fake.setInstallErrorCode(InstallErrorCode.ERROR_UNKNOWN)
        holder.checkForUpdate()

        assertEquals(UpdateAvailability.Unknown, holder.availability)
        assertNotNull(holder.lastError)
    }

    @Test
    fun stalenessNullReturnsFalse() {
        fake.setUpdateAvailable(1)
        holder.checkForUpdate()

        assertNull(holder.stalenessDays)
        assertFalse(holder.hasNewVersionAvailableForMoreThan(3.days))
    }

    @Test
    fun stalenessBoundaryAtExactlyNDays() {
        fake.setUpdateAvailable(1)
        fake.setClientVersionStalenessDays(3)
        holder.checkForUpdate()

        assertEquals(3, holder.stalenessDays)
        assertTrue(holder.hasNewVersionAvailableForMoreThan(3.days))
        assertFalse(holder.hasNewVersionAvailableForMoreThan(4.days))
    }

    @Test
    fun stalenessBelowThresholdReturnsFalse() {
        fake.setUpdateAvailable(1)
        fake.setClientVersionStalenessDays(2)
        holder.checkForUpdate()

        assertFalse(holder.hasNewVersionAvailableForMoreThan(3.days))
    }

    @Test
    fun flexibleUpdateLaunchesFlexibleFlow() {
        fake.setUpdateAvailable(1)
        holder.checkForUpdate()
        holder.startFlexibleUpdate()

        assertTrue(fake.isConfirmationDialogVisible)
        assertFalse(fake.isImmediateFlowVisible)
    }

    @Test
    fun immediateUpdateLaunchesImmediateFlow() {
        fake.setUpdateAvailable(1)
        holder.checkForUpdate()
        holder.startImmediateUpdate()

        assertTrue(fake.isImmediateFlowVisible)
        assertFalse(fake.isConfirmationDialogVisible)
    }
}
