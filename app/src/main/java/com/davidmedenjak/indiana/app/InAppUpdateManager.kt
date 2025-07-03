package com.davidmedenjak.indiana.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppUpdateManager @Inject constructor(
    private val context: Application,
) : DefaultLifecycleObserver {
    
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var activity: Activity
    private lateinit var updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    
    private val _updateState = MutableStateFlow(UpdateState.UNKNOWN)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()
    
    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo.asStateFlow()
    
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        handleInstallState(state)
    }
    
    init {
        appUpdateManager = AppUpdateManagerFactory.create(context)
        appUpdateManager.registerListener(installStateUpdatedListener)
    }
    
    fun initialize(activity: Activity, updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        this.activity = activity
        this.updateResultLauncher = updateResultLauncher
    }
    
    suspend fun checkForUpdate() {
        try {
            val appUpdateInfo = appUpdateManager.appUpdateInfo.await()
            
            _updateInfo.value = appUpdateInfo
            
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    _updateState.value = UpdateState.AVAILABLE
                    Log.d("InAppUpdate", "Update available, staleness: ${appUpdateInfo.clientVersionStalenessDays()} days")
                }
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    _updateState.value = UpdateState.IN_PROGRESS
                    Log.d("InAppUpdate", "Update in progress")
                }
                else -> {
                    _updateState.value = UpdateState.NOT_AVAILABLE
                    Log.d("InAppUpdate", "No update available")
                }
            }
        } catch (e: Exception) {
            Log.e("InAppUpdate", "Error checking for update", e)
            _updateState.value = UpdateState.ERROR
        }
    }
    
    fun startFlexibleUpdate() {
        val appUpdateInfo = _updateInfo.value ?: return
        
        try {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                updateResultLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            )
        } catch (e: Exception) {
            Log.e("InAppUpdate", "Error starting flexible update", e)
        }
    }
    
    fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }
    
    fun isUpdateAvailableForMoreThanThreeDays(): Boolean {
        val appUpdateInfo = _updateInfo.value ?: return false
        val stalenessDays = appUpdateInfo.clientVersionStalenessDays() ?: return false
        return stalenessDays >= 3
    }
    
    private fun handleInstallState(state: InstallState) {
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                _updateState.value = UpdateState.DOWNLOADED
                Log.d("InAppUpdate", "Update downloaded, ready to install")
            }
            InstallStatus.DOWNLOADING -> {
                _updateState.value = UpdateState.DOWNLOADING
                Log.d("InAppUpdate", "Update downloading")
            }
            InstallStatus.FAILED -> {
                _updateState.value = UpdateState.ERROR
                Log.e("InAppUpdate", "Update failed")
            }
            InstallStatus.INSTALLED -> {
                _updateState.value = UpdateState.INSTALLED
                Log.d("InAppUpdate", "Update installed")
            }
            else -> {
                Log.d("InAppUpdate", "Install state: ${state.installStatus()}")
            }
        }
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }
    
    enum class UpdateState {
        UNKNOWN,
        NOT_AVAILABLE,
        AVAILABLE,
        IN_PROGRESS,
        DOWNLOADING,
        DOWNLOADED,
        INSTALLED,
        ERROR
    }
}